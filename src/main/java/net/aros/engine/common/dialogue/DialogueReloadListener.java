package net.aros.engine.common.dialogue;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.util.AeUtil;

import net.aros.haru.Haru;
import net.aros.haru.compile.CompiledProgram;
import net.aros.haru.compile.io.ProgramIO;
import net.aros.haru.diagnostic.DiagnosticCollector;
import net.aros.haru.diagnostic.DiagnosticRenderer;
import net.aros.haru.diagnostic.Severity;
import net.aros.haru.link.LinkedProgram;
import net.aros.haru.link.Linker;
import net.aros.haru.translation.TranslationFile;
import net.aros.haru.translation.io.TranslationFileIO;
import net.aros.haru.value.Value;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.MinecraftServer;
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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class DialogueReloadListener extends SimplePreparableReloadListener<DialogueReloadListener.LoadResult> {
    private static final String CACHE_DIR = "compiled_dialogues";
    private static final String DIR = "dialogue";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROJECTS_DIR = "projects";
    private static final String SOURCE_EXTENSION = ".haru";
    private static final String BINARY_EXTENSION = ".haruc";
    private static final String TRANSLATIONS_EXTENSION = ".tr.json";
    private static final FileToIdConverter SOURCE_CONV = new FileToIdConverter(DIR, SOURCE_EXTENSION);
    private static final FileToIdConverter TRANSLATIONS_CONV = new FileToIdConverter(DIR, TRANSLATIONS_EXTENSION);

    public record LoadResult(Map<ResourceLocation, Dialogue> dialogues, DialogueLoadReport report) {}

    private static volatile DialogueLoadReport lastReport;
    private static DialogueLoadReport notifiedReport;
    private static final Set<UUID> NOTIFIED = new HashSet<>();
    private final List<DialogueLoadReport.Entry> issues = new ArrayList<>();
    private final Map<ResourceLocation, String> origins = new HashMap<>();

    public static @Nullable DialogueLoadReport lastReport() {
        return lastReport;
    }

    /** Called on the server thread; also delivers startup reports to admins logging in later. */
    public static void notifyAdmins(MinecraftServer server) {
        DialogueLoadReport report = lastReport;
        if (notifiedReport != report) {
            NOTIFIED.clear();
            notifiedReport = report;
        }
        if (report == null || report.errors() == 0) return;
        for (var player : server.getPlayerList().getPlayers()) {
            if (!player.createCommandSourceStack().hasPermission(2) || NOTIFIED.contains(player.getUUID())) continue;
            player.sendSystemMessage(ArosEngine.translation("commands", "dialogue.status.errors",
                    report.errors(), DialogueLoadReport.statusLink()).withStyle(net.minecraft.ChatFormatting.RED));
            NOTIFIED.add(player.getUUID());
        }
    }

    public static void clearReport() {
        lastReport = null;
        notifiedReport = null;
        NOTIFIED.clear();
    }

    private void error(String message, Object... args) {
        LOGGER.error(message, args);
        issues.add(new DialogueLoadReport.Entry(Severity.ERROR,
                "error: " + org.slf4j.helpers.MessageFormatter.arrayFormat(message, args).getMessage()));
    }

    private void warn(String message, Object... args) {
        LOGGER.warn(message, args);
        issues.add(new DialogueLoadReport.Entry(Severity.WARNING,
                "warning: " + org.slf4j.helpers.MessageFormatter.arrayFormat(message, args).getMessage()));
    }

    @Override
    protected @NotNull LoadResult prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        issues.clear();
        origins.clear();
        Map<ResourceLocation, Source> sources = new TreeMap<>();
        collectProjectSources(sources);
        collectDataSources(resourceManager, sources);

        return load(resourceManager, sources, false, "Reload");
    }

    /** Fresh compilation of this project only, ignoring binaries and installed datapack overrides. */
    public static DialogueLoadReport compileProject(ResourceManager manager, String namespace) {
        if (!ResourceLocation.isValidNamespace(namespace) || namespace.equals(".") || namespace.equals("..")) {
            throw new IllegalArgumentException("Invalid project namespace: " + namespace);
        }
        var loader = new DialogueReloadListener();
        Map<ResourceLocation, Source> sources = new TreeMap<>();
        Path projects = AeUtil.getCacheDir().resolve(DIR).resolve(PROJECTS_DIR);
        Path project = projects.resolve(namespace);
        if (!Files.isDirectory(project, LinkOption.NOFOLLOW_LINKS)) {
            loader.error("Dialogue project does not exist: {}", namespace);
        } else loader.collectProjectSources(projects, project, sources);
        return loader.load(manager, sources, true, "Export: " + namespace).report();
    }

    private LoadResult load(ResourceManager resourceManager, Map<ResourceLocation, Source> sources,
                            boolean forceCompile, String operation) {
        Map<ResourceLocation, Dialogue> dialogues = new HashMap<>();
        sources.forEach((id, source) -> {
            origins.put(id, source.origin());
            int previousIssues = issues.size();
            try {
                var program = loadOrCompile(resourceManager, source, forceCompile).orElse(null);
                if (program == null) {
                    if (!source.diagnosticCollector().hasErrors()
                            && issues.subList(previousIssues, issues.size()).stream().noneMatch(issue -> issue.severity() == Severity.ERROR)) {
                        error("Failed to load dialogue '{}'", id);
                    }
                    return;
                }
                dialogues.put(id, new Dialogue(id, source.dialect(), metadata(source, program), program));
            } catch (Exception e) {
                error("Failed to load dialogue '{}': {}", id, e.toString(), e);
            } finally {
                renderDiagnostics(source);
            }
        });
        return new LoadResult(Map.copyOf(dialogues), new DialogueLoadReport(operation, dialogues.size(), issues, origins));
    }

    private @NotNull DialogueMetadata metadata(Source source, LinkedProgram program) {
        Map<String, Value> exports = program.program().exports();
        String audiencePolicy = stringValue(exports.get("on_audience_change"), source, program);
        if (audiencePolicy != null && Arrays.stream(OnAudienceChange.values())
                .noneMatch(policy -> policy.name().equalsIgnoreCase(audiencePolicy))) {
            error("Dialogue '{}': invalid on_audience_change '{}' (expected restart, cancel or continue)", source.id(), audiencePolicy);
        }
        OnAudienceChange policy = OnAudienceChange.parse(
                audiencePolicy,
                OnAudienceChange.parse(ArosEngine.CONFIG.dialogue.defaultOnAudienceChange(), OnAudienceChange.CONTINUE)
        );
        Value priority = exports.get("priority");
        return new DialogueMetadata(priorityValue(priority, source), policy);
    }

    private int priorityValue(Value value, Source source) {
        if (value == null) return DialogueMetadata.DEFAULT_PRIORITY;
        if (value.isNumber()) return (int) Math.round(value.asNumber());
        error("Dialogue \"{}\": 'priority' must be a number, falling back to the default", source.id());
        return DialogueMetadata.DEFAULT_PRIORITY;
    }

    private @Nullable String stringValue(Value value, Source source, LinkedProgram program) {
        if (value == null) return null;
        if (value.isLiteralRef()) return program.program().literals()[value.asLiteralRef()];
        error("Dialogue \"{}\": 'on_audience_change' must be a string literal, falling back to the default", source.id());
        return null;
    }

    @Override
    protected void apply(LoadResult object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Dialogues.replace(object.dialogues());
        lastReport = object.report();
    }

    private void collectProjectSources(Map<ResourceLocation, Source> sources) {
        Path projectsDir = AeUtil.getCacheDir().resolve(DIR).resolve(PROJECTS_DIR);
        collectProjectSources(projectsDir, projectsDir, sources);
    }

    private void collectProjectSources(Path projectsDir, Path root, Map<ResourceLocation, Source> sources) {
        if (Files.notExists(root)) return;

        try (Stream<Path> files = Files.walk(root)) {
            files.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(file -> file.getFileName().toString().endsWith(SOURCE_EXTENSION))
                    .sorted()
                    .forEach(file -> readProjectSource(projectsDir, file, sources));
        } catch (Exception e) {
            error("Failed to scan dialogue projects: {}", e.toString(), e);
        }
    }

    private void readProjectSource(Path projectsDir, Path file, Map<ResourceLocation, Source> sources) {
        Path relative = projectsDir.relativize(file);
        if (relative.getNameCount() < 2) {
            error("Skipping dialogue project file \"{}\". Expected layout: <modid>/<name>.<dialect>.haru", file);
            return;
        }

        String namespace = relative.getName(0).toString();
        String path = relative.subpath(1, relative.getNameCount()).toString().replace('\\', '/');
        path = path.substring(0, path.length() - SOURCE_EXTENSION.length());

        ResourceLocation id = ResourceLocation.tryBuild(namespace, path);
        if (id == null) {
            error("Skipping dialogue project file \"{}\". Invalid dialogue id \"{}:{}\"", file, namespace, path);
            return;
        }

        Source source = readSource(id, file, file.toString(), () -> Files.newInputStream(file));
        if (source != null) sources.put(id, source);
    }

    private void collectDataSources(ResourceManager resourceManager, Map<ResourceLocation, Source> sources) {
        SOURCE_CONV.listMatchingResources(resourceManager).forEach((fileKey, resource) -> {
            ResourceLocation id = SOURCE_CONV.fileToId(fileKey);
            Source source = readSource(id, null, resource.sourcePackId() + ": data/" + fileKey.getNamespace() + "/" + fileKey.getPath(), resource::open);
            if (source == null) return;

            if (sources.containsKey(id)) {
                warn("Dialogue \"{}\" is defined both in a project and in data. Loading from data", id);
            }
            sources.put(id, source);
        });
    }

    private Optional<LinkedProgram> loadOrCompile(ResourceManager resourceManager, Source source, boolean forceCompile) {
        int previousIssues = issues.size();
        TranslationFile translationFile = translationFile(resourceManager, source);
        // Never overwrite a broken translation file with newly generated translations.
        if (issues.subList(previousIssues, issues.size()).stream().anyMatch(issue -> issue.severity() == Severity.ERROR)) {
            return Optional.empty();
        }
        if (translationFile == null) {
            if (!source.isFromProject()) {
                ResourceLocation key = TRANSLATIONS_CONV.idToFile(source.id());
                warn("Dialogue \"{}\" is not translated. Expected \"data/{}/{}\" next to the dialogue",
                        source.id(), key.getNamespace(), key.getPath());
            }
            return compileAndLink(source, null);
        }
        CompiledProgram program = forceCompile ? null : tryImport(source, translationFile);
        if (program == null) {
            return compileAndLink(source, translationFile);
        }
        return link(source, program);
    }

    private Optional<LinkedProgram> compileAndLink(Source source, @Nullable TranslationFile oldTranslationFile) {
        LOGGER.debug("Compiling dialogue \"{}\"", source.id());
        CompiledProgram compiledProgram = Haru
                .compile(source.text(), source.diagnosticCollector(), null, oldTranslationFile)
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
        Linker linker = new Linker(source.diagnosticCollector(), program).defineAll(source.dialect().functions());
        LinkedProgram linkedProgram = linker.link();
        if (source.diagnosticCollector().hasErrors() || linkedProgram == null) {
            LOGGER.error("Failed to link \"{}\"", source.id());
            return Optional.empty();
        }
        return Optional.of(linkedProgram);
    }

    private void renderDiagnostics(Source source) {
        Path path = source.sourceFile() == null
                ? Path.of("data", source.id().getNamespace(), DIR, source.id().getPath() + SOURCE_EXTENSION)
                : source.sourceFile();
        if (source.diagnosticCollector().diagnostics().isEmpty()) return;
        System.out.println(DiagnosticRenderer.render(source.diagnosticCollector().diagnostics(), path, source.text()));
        for (var diagnostic : source.diagnosticCollector().diagnostics()) {
            issues.add(DialogueLoadReport.Entry.diagnostic(diagnostic, path, source.text()));
        }
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
            error("Failed to write translations for '{}': {}", source.id(), e.toString(), e);
        }
    }

    private @Nullable TranslationFile importTranslationFile(Source source, InputStreamSupplier opener) {
        try (InputStream is = opener.get()) {
            TranslationFile file = TranslationFileIO.importFile(is).orElse(null);
            if (file == null) error("Failed to import translations for '{}'", source.id());
            return file;
        } catch (Exception e) {
            error("Failed to import translations for '{}': {}", source.id(), e.toString(), e);
        }
        return null;
    }

    private void cacheBinary(Source source, CompiledProgram program) {
        try {
            Path binaryPath = getCachePathFor(source.id(), BINARY_EXTENSION, true);
            try (OutputStream os = Files.newOutputStream(binaryPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ProgramIO.exportFile(source.text(), program, os);
            }
        } catch (Exception e) {
            warn("Failed to cache binary for '{}': {}", source.id(), e.toString(), e);
        }
    }

    private @NotNull Path getCachePathFor(ResourceLocation id, String ext, boolean mkdirs) {
        ResourceLocation fileKey = id.withPath(p -> p + ext);
        Path dir = AeUtil.getCacheDir().resolve(CACHE_DIR).resolve(fileKey.getNamespace());
        Path path = dir.resolve(fileKey.getPath());
        if (mkdirs) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return path;
    }

    @Nullable
    private Source readSource(ResourceLocation id, @Nullable Path sourceFile, String origin, InputStreamSupplier opener) {
        origins.put(id, origin);
        String text = readSourceText(id, opener);
        if (text == null) return null;

        DialogueDialect dialect = dialect(id);
        if (dialect == null) return null;

        return new Source(id, sourceFile, origin, text, dialect, new DiagnosticCollector());
    }

    @Nullable
    private String readSourceText(ResourceLocation id, InputStreamSupplier opener) {
        try (InputStream is = opener.get()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            error("Failed to read text file of dialogue '{}': {}", id, e.toString(), e);
            return null;
        }
    }

    private @Nullable DialogueDialect dialect(ResourceLocation id) {
        String dialectName = dialectFromPath(id.getPath());
        if (dialectName == null) {
            error("Dialogue \"{}\" has no dialect. Expected to be <name>.<dialect>.haru", id);
            return null;
        }

        DialogueDialect dialect = DialogueDialects.get(dialectName).orElse(null);
        if (dialect == null) {
            error("Dialect \"{}\" was not found. Skipping {}", dialectName, id);
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

    private record Source(ResourceLocation id, @Nullable Path sourceFile, String origin, String text, DialogueDialect dialect,
                          DiagnosticCollector diagnosticCollector) {

        private boolean isFromProject() {
            return sourceFile != null;
        }
    }

    private interface InputStreamSupplier {
        InputStream get() throws Exception;
    }
}
