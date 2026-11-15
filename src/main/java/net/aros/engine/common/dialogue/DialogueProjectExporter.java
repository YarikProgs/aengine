package net.aros.engine.common.dialogue;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Exports project sources and translations, never compiled dialogue caches. */
public final class DialogueProjectExporter {
    private DialogueProjectExporter() {
    }

    /** The target must not exist. Both formats have the same datapack layout. */
    public static void export(Path project, Path target, String namespace, boolean archive) throws IOException {
        export(project, target, namespace, archive, false);
    }

    /** Build completely before replacing an explicitly approved previous export. */
    public static void export(Path project, Path target, String namespace, boolean archive, boolean overwrite) throws IOException {
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS) && !overwrite) {
            throw new FileAlreadyExistsException(target.toString());
        }
        Files.createDirectories(target.getParent());
        Path staging = Files.createTempDirectory(target.getParent(), ".dialogue-export-");
        Path output = staging.resolve(archive ? "output.zip" : "output");
        Path backup = staging.resolve("previous");
        boolean preserveBackup = false;
        try {
            write(project, output, namespace, archive);
            if (overwrite && Files.exists(target, LinkOption.NOFOLLOW_LINKS)) Files.move(target, backup);
            try {
                Files.move(output, target);
            } catch (IOException e) {
                if (Files.exists(backup, LinkOption.NOFOLLOW_LINKS)) {
                    try {
                        Files.move(backup, target);
                    } catch (IOException restoreFailure) {
                        preserveBackup = true;
                        e.addSuppressed(restoreFailure);
                    }
                }
                throw e;
            }
        } finally {
            // If restoration failed, keep the old export in staging for recovery.
            if (!preserveBackup) removeGeneratedTree(staging);
        }
    }

    private static void removeGeneratedTree(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
        }
    }

    private static void write(Path project, Path target, String namespace, boolean archive) throws IOException {
        if (!ResourceLocation.isValidNamespace(namespace) || namespace.equals(".") || namespace.equals("..")) {
            throw new IllegalArgumentException("Invalid project namespace: " + namespace);
        }
        if (!Files.isDirectory(project, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Dialogue project is not a directory: " + project);
        }
        List<Path> files;
        try (var paths = Files.walk(project)) {
            files = paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(path -> path.getFileName().toString().endsWith(".haru")
                            || path.getFileName().toString().endsWith(".tr.json"))
                    .sorted()
                    .toList();
        }

        byte[] metadata = packMetadata(namespace).getBytes(StandardCharsets.UTF_8);
        String prefix = "data/" + namespace + "/dialogue/";
        Files.createDirectories(target.getParent());
        if (archive) {
            try (var zip = new ZipOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE_NEW))) {
                zip.putNextEntry(new ZipEntry("pack.mcmeta"));
                zip.write(metadata);
                zip.closeEntry();
                for (Path file : files) {
                    zip.putNextEntry(new ZipEntry(prefix + project.relativize(file).toString().replace('\\', '/')));
                    Files.copy(file, zip);
                    zip.closeEntry();
                }
            }
        } else {
            Files.createDirectory(target);
            Files.write(target.resolve("pack.mcmeta"), metadata, StandardOpenOption.CREATE_NEW);
            Path data = target.resolve(prefix);
            Files.createDirectories(data);
            for (Path file : files) {
                Path destination = data.resolve(project.relativize(file));
                Files.createDirectories(destination.getParent());
                Files.copy(file, destination);
            }
        }
    }

    private static String packMetadata(String namespace) {
        var pack = new JsonObject();
        pack.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
        pack.addProperty("description", "Dialogue project " + namespace);
        var root = new JsonObject();
        root.add("pack", pack);
        return new GsonBuilder().setPrettyPrinting().create().toJson(root) + "\n";
    }
}
