package net.aros.engine.common.dialogue;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.util.AeUtil;
import net.aros.haru.Haru;
import net.aros.haru.compile.CompiledProgram;
import net.aros.haru.compile.io.ProgramIO;
import net.aros.haru.diagnostic.DiagnosticCollector;
import net.aros.haru.diagnostic.DiagnosticRenderer;
import net.aros.haru.link.LinkedProgram;
import net.aros.haru.link.Linker;
import net.aros.haru.translation.TranslationFile;
import net.aros.haru.translation.io.TranslationFileIO;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class DialogueReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, Dialogue>> {
    private static final String CACHE_DIR = "compiled_dialogues";
    private static final String DIR = "dialogue";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROJECTS_DIR = "projects";
    private static final String SOURCE_EXTENSION = ".haru";
    private static final String BINARY_EXTENSION = ".haruc";
    private static final String TRANSLATIONS_EXTENSION = ".tr.json";
    private static final FileToIdConverter SOURCE_CONV = new FileToIdConverter(DIR, SOURCE_EXTENSION);
    private static final FileToIdConverter TRANSLATIONS_CONV = new FileToIdConverter(DIR, TRANSLATIONS_EXTENSION);

    @Override
    protected @NotNull Map<ResourceLocation, Dialogue> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Source> sources = new HashMap<>();
        collectProjectSources(sources);
        collectDataSources(resourceManager, sources);

        Map<ResourceLocation, Dialogue> dialogues = new HashMap<>();
        sources.forEach((id, source) -> {
            var program = loadOrCompile(resourceManager, source).orElse(null);
            if (program == null) {
                renderDiagnostics(source);
                return;
            }

            dialogues.put(id, new Dialogue(id, program));
            renderDiagnostics(source);
        });
        return dialogues;
    }

    @Override
    protected void apply(Map<ResourceLocation, Dialogue> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Dialogues.replace(object);
    }

    private void collectProjectSources(Map<ResourceLocation, Source> sources) {
        Path projectsDir = AeUtil.getCacheDir().resolve(DIR).resolve(PROJECTS_DIR);
        if (Files.notExists(projectsDir)) return;

        try (Stream<Path> files = Files.walk(projectsDir)) {
            files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(SOURCE_EXTENSION))
                    .sorted()
                    .forEach(file -> readProjectSource(projectsDir, file, sources));
        } catch (Exception e) {
            LOGGER.error("Failed to scan dialogue projects", e);
        }
    }

    private void readProjectSource(Path projectsDir, Path file, Map<ResourceLocation, Source> sources) {
        Path relative = projectsDir.relativize(file);
        if (relative.getNameCount() < 2) {
            LOGGER.error("Skipping dialogue project file \"{}\". Expected layout: <modid>/<name>.<dialect>.haru", file);
            return;
        }

        String namespace = relative.getName(0).toString();
        String path = relative.subpath(1, relative.getNameCount()).toString().replace('\\', '/');
        path = path.substring(0, path.length() - SOURCE_EXTENSION.length());

        ResourceLocation id = ResourceLocation.tryBuild(namespace, path);
        if (id == null) {
            LOGGER.error("Skipping dialogue project file \"{}\". Invalid dialogue id \"{}:{}\"", file, namespace, path);
            return;
        }

        Source source = readSource(id, file, () -> Files.newInputStream(file));
        if (source != null) sources.put(id, source);
    }

    private void collectDataSources(ResourceManager resourceManager, Map<ResourceLocation, Source> sources) {
        SOURCE_CONV.listMatchingResources(resourceManager).forEach((fileKey, resource) -> {
            ResourceLocation id = SOURCE_CONV.fileToId(fileKey);
            Source source = readSource(id, null, resource::open);
            if (source == null) return;

            if (sources.containsKey(id)) {
                LOGGER.warn("Dialogue \"{}\" is defined both in a project and in data. Loading from data", id);
            }
            sources.put(id, source);
        });
    }

    private Optional<LinkedProgram> loadOrCompile(ResourceManager resourceManager, Source source) {
        TranslationFile translationFile = translationFile(resourceManager, source);
        if (translationFile == null) {
            if (!source.isFromProject()) {
                ResourceLocation key = TRANSLATIONS_CONV.idToFile(source.id());
                LOGGER.warn("Dialogue \"{}\" is not translated. Expected \"data/{}/{}\" next to the dialogue",
                        source.id(), key.getNamespace(), key.getPath());
            }
            return compileAndLink(source, null);
        }
        CompiledProgram program = tryImport(source, translationFile);
        if (program == null) {
            return compileAndLink(source, translationFile);
        }
        return link(source, program);
    }

    private Optional<LinkedProgram> compileAndLink(Source source, @Nullable TranslationFile oldTranslationFile) {
        LOGGER.debug("Compiling dialogue \"{}\"", source.id());
        CompiledProgram compiledProgram = Haru
                .compile(source.text(), source.diagnosticCollector(), null, source.dialect()::setupCompiler, oldTranslationFile)
                .orElse(null);
        if (source.diagnosticCollector().hasErrors() || compiledProgram == null) {
            return Optional.empty();
        }

        Optional<LinkedProgram> linkedProgram = link(source, compiledProgram);
        if (linkedProgram.isPresent()) {
            cacheBinary(source, compiledProgram);
            if (source.isFromProject()) {
                writeTranslation(source, compiledProgram.translationFile());
            }
        }
        return linkedProgram;
    }

    private @NotNull Optional<LinkedProgram> link(Source source, CompiledProgram program) {
        LinkedProgram linkedProgram = source.dialect().setupLinker(new Linker(source.diagnosticCollector(), program)).link();
        if (source.diagnosticCollector().hasErrors() || linkedProgram == null) {
            LOGGER.error("Failed to link \"{}\"", source.id());
            return Optional.empty();
        }
        return Optional.of(linkedProgram);
    }

    private void renderDiagnostics(Source source) {
        System.out.println(DiagnosticRenderer.render(source.diagnosticCollector().diagnostics(), Path.of(source.id().toString()), source.text()));
    }

    private @Nullable CompiledProgram tryImport(Source source, TranslationFile translationFile) {
        Path binary = getCachePathFor(source.id(), BINARY_EXTENSION, false);
        if (Files.notExists(binary)) {
            return null;
        }
        try (InputStream is = Files.newInputStream(binary)) {
            if (!ProgramIO.isFreshHeader(source.text(), is, new DiagnosticCollector()))
                return null;
            return ProgramIO.importFile(translationFile, is);
        } catch (Exception ignore) {
            LOGGER.debug("Failed to import binary for \"{}\"", source.id());
        }
        return null;
    }

    private @Nullable TranslationFile translationFile(ResourceManager resourceManager, Source source) {
        if (source.isFromProject()) {
            Path translation = siblingTranslation(source.sourceFile());
            if (Files.notExists(translation)) {
                return null;
            }
            return importTranslationFile(source, () -> Files.newInputStream(translation));
        }

        var res = resourceManager.getResource(TRANSLATIONS_CONV.idToFile(source.id()))
                .orElse(null);
        if (res == null) {
            return null;
        }
        return importTranslationFile(source, res::open);
    }

    private @NotNull Path siblingTranslation(@NotNull Path sourceFile) {
        String name = sourceFile.getFileName().toString();
        return sourceFile.resolveSibling(name.substring(0, name.length() - SOURCE_EXTENSION.length()) + TRANSLATIONS_EXTENSION);
    }

    private void writeTranslation(Source source, TranslationFile translationFile) {
        Path path = siblingTranslation(source.sourceFile());
        try (OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            TranslationFileIO.exportFile(translationFile, os);
        } catch (Exception e) {
            LOGGER.error("Failed to write translations for \"{}\"", source.id(), e);
        }
    }

    private @Nullable TranslationFile importTranslationFile(Source source, InputStreamSupplier opener) {
        try (InputStream is = opener.get()) {
            return TranslationFileIO.importFile(is).orElse(null);
        } catch (Exception ignore) {
            LOGGER.debug("Failed to import translations for \"{}\"", source.id());
        }
        return null;
    }

    private void cacheBinary(Source source, CompiledProgram program) {
        Path binaryPath = getCachePathFor(source.id(), BINARY_EXTENSION, true);
        try (OutputStream os = Files.newOutputStream(binaryPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            ProgramIO.exportFile(source.text(), program, os);
        } catch (Exception e) {
            LOGGER.error("Failed to cache binary for \"{}\"", source.id(), e);
        }
    }

    private @NotNull Path getCachePathFor(ResourceLocation id, String ext, boolean mkdirs) {
        ResourceLocation fileKey = id.withPath(p -> p + ext);
        Path dir = AeUtil.getCacheDir().resolve(CACHE_DIR).resolve(fileKey.getNamespace());
        if (mkdirs) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir.resolve(fileKey.getPath());
    }

    @Nullable
    private Source readSource(ResourceLocation id, @Nullable Path sourceFile, InputStreamSupplier opener) {
        String text = readSourceText(id, opener);
        if (text == null) return null;

        DialogueDialect dialect = dialect(id);
        if (dialect == null) return null;

        return new Source(id, sourceFile, text, dialect, new DiagnosticCollector());
    }

    @Nullable
    private String readSourceText(ResourceLocation id, InputStreamSupplier opener) {
        try (InputStream is = opener.get()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Failed to read text file of dialogue \"{}\"", id, e);
            return null;
        }
    }

    private @Nullable DialogueDialect dialect(ResourceLocation id) {
        String dialectName = dialectFromPath(id.getPath());
        if (dialectName == null) {
            LOGGER.error("Dialogue \"{}\" has no dialect. Expected to be <name>.<dialect>.haru", id);
            return null;
        }

        DialogueDialect dialect = DialogueDialects.get(dialectName).orElse(null);
        if (dialect == null) {
            LOGGER.error("Dialect \"{}\" was not found. Skipping {}", dialectName, id);
            return null;
        }
        return dialect;
    }

    @Nullable
    private String dialectFromPath(String path) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1) return null;

        String dialect = path.substring(dotIndex + 1);
        return dialect.isEmpty() ? null : dialect;
    }

    private record Source(ResourceLocation id, @Nullable Path sourceFile, String text, DialogueDialect dialect,
                          DiagnosticCollector diagnosticCollector) {

        private boolean isFromProject() {
            return sourceFile != null;
        }
    }

    private interface InputStreamSupplier {
        InputStream get() throws Exception;
    }
}
