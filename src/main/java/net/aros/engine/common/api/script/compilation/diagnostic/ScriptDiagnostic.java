package net.aros.engine.common.api.script.compilation.diagnostic;

public record ScriptDiagnostic(String severity, String message, String rendered, Throwable exception) {
    public boolean isError() {
        return "ERROR".equals(severity) || "FATAL".equals(severity);
    }

    public String formatted() {
        return rendered == null || rendered.isBlank() ? "%s: %s".formatted(severity, message) : rendered;
    }
}
