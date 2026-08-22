package net.aros.engine.common.script.backend;

import net.aros.engine.common.api.script.compilation.backend.ScriptBackendProvider;
import net.aros.engine.common.script.backend.kotlin.KotlinScriptBackend;

import java.util.List;

public final class ScriptBackendProviders {
    private static final ScriptBackendProvider KOTLIN = ScriptBackendProvider.of(
            KotlinScriptBackend.LANGUAGE,
            KotlinScriptBackend::get
    );

    private ScriptBackendProviders() {
    }

    public static ScriptBackendProvider kotlin() {
        return KOTLIN;
    }

    public static List<ScriptBackendProvider> defaults() {
        return List.of(kotlin());
    }
}
