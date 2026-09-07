package net.aros.engine.common.api.script.compilation;

import java.util.Objects;

public record ScriptLanguage(String id, String fileExtension) {
    public ScriptLanguage {
        id = requireToken(id, "id");
        fileExtension = normalizeExtension(fileExtension);
    }

    private static String requireToken(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeExtension(String extension) {
        Objects.requireNonNull(extension, "fileExtension");
        String normalized = extension.trim();
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
