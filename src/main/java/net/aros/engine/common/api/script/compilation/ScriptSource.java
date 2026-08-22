package net.aros.engine.common.api.script.compilation;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ScriptSource(ResourceLocation path, ScriptLanguage language, String contents) {
    public ScriptSource {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(contents, "contents");
    }
}
