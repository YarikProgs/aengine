package net.aros.engine.common.api.script.compilation.exception;

import net.aros.engine.common.api.script.compilation.ScriptSource;
import net.aros.engine.common.api.script.compilation.diagnostic.ScriptDiagnostic;

import java.util.List;
import java.util.Objects;

public class ScriptCompilationException extends RuntimeException {
    public ScriptCompilationException(ScriptSource source, List<ScriptDiagnostic> diagnostics) {
        super("%s script '%s' failed to compile:%n%s"
                .formatted(
                        Objects.requireNonNull(source, "source").language().id(),
                        source.path(),
                        formatDiagnostics(Objects.requireNonNull(diagnostics, "diagnostics"))
                ));
    }

    static String formatDiagnostics(List<ScriptDiagnostic> diagnostics) {
        Objects.requireNonNull(diagnostics, "diagnostics");
        if (diagnostics.isEmpty()) return "<no diagnostics>";
        return diagnostics.stream()
                .map(ScriptDiagnostic::formatted)
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("<no diagnostics>");
    }
}
