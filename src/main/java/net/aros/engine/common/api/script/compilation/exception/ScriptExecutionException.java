package net.aros.engine.common.api.script.compilation.exception;

import net.aros.engine.common.api.script.compilation.ScriptSource;
import net.aros.engine.common.api.script.compilation.diagnostic.ScriptDiagnostic;

import java.util.List;
import java.util.Objects;

public class ScriptExecutionException extends RuntimeException {
    public ScriptExecutionException(ScriptSource source, List<ScriptDiagnostic> diagnostics) {
        super("%s script '%s' failed to run:%n%s"
                .formatted(
                        Objects.requireNonNull(source, "source").language().id(),
                        source.path(),
                        ScriptCompilationException.formatDiagnostics(Objects.requireNonNull(diagnostics, "diagnostics"))
                ));
    }
}
