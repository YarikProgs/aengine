package net.aros.engine.common.api.script.compilation.backend;

import net.aros.engine.common.api.script.compilation.ScriptLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public interface ScriptBackendProvider {
    @NotNull ScriptLanguage language();

    @NotNull ScriptBackend backend();

    static @NotNull ScriptBackendProvider of(
            @NotNull ScriptLanguage language,
            @NotNull Supplier<? extends ScriptBackend> backendSupplier
    ) {
        return new SuppliedScriptBackendProvider(language, backendSupplier);
    }

    record SuppliedScriptBackendProvider(
            @NotNull ScriptLanguage language,
            @NotNull Supplier<? extends ScriptBackend> backendSupplier
    ) implements ScriptBackendProvider {
        public SuppliedScriptBackendProvider {
            Objects.requireNonNull(language, "language");
            Objects.requireNonNull(backendSupplier, "backendSupplier");
        }

        @Override
        public @NotNull ScriptBackend backend() {
            ScriptBackend backend = Objects.requireNonNull(backendSupplier.get(), "backend");
            if (!backend.language().equals(language)) {
                throw new IllegalStateException(
                        "Script backend language mismatch: expected %s, got %s"
                                .formatted(language.id(), backend.language().id())
                );
            }
            return backend;
        }
    }
}
