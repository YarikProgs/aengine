package net.aros.engine.common.api.script.compilation;

import java.util.Objects;

public record AeScript(ScriptLanguage language, Runnable runnable) implements Runnable {
    public AeScript {
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(runnable, "runnable");
    }

    @Override
    public void run() {
        runnable.run();
    }
}
