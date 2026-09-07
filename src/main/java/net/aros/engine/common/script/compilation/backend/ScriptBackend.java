package net.aros.engine.common.script.compilation.backend;

import net.aros.engine.common.script.compilation.AeScript;
import net.aros.engine.common.script.compilation.ScriptLanguage;
import net.aros.engine.common.script.compilation.ScriptSource;
import net.aros.engine.common.script.compilation.exception.ScriptCompilationException;
import org.jetbrains.annotations.NotNull;

public interface ScriptBackend {
    @NotNull ScriptLanguage language();

    @NotNull AeScript compile(@NotNull ScriptSource source) throws ScriptCompilationException;
}
