package net.aros.engine.common.api.script;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.api.script.compilation.ScriptLanguage;
import net.aros.engine.common.api.script.compilation.backend.ScriptBackend;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScriptBackends {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ScriptLanguage, ScriptBackend> BACKENDS = new HashMap<>();

    public static void register(@NotNull ScriptBackend backend) {
        if (BACKENDS.containsKey(backend.language())) {
            ScriptLanguage lang = backend.language();
            LOGGER.error("Refusing to register second backend for the '{}' language (.{})", lang.id(), lang.fileExtension());
            return;
        }
        BACKENDS.put(backend.language(), backend);
    }

    public static @NotNull @UnmodifiableView Collection<ScriptBackend> backends() {
        return Collections.unmodifiableCollection(BACKENDS.values());
    }
}
