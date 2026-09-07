package net.aros.engine.common.script.procedure;

public class ProcedureException extends Exception {
    public ProcedureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcedureException(String message) {
        super(message);
    }
}
