package net.aros.engine.common.dialogue;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.*;

public class DialogueDialects {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, DialogueDialect> DIALECTS = new HashMap<>();

    public static void register(@NotNull DialogueDialect dialect) {
        if (DIALECTS.containsKey(dialect.name())) {
            LOGGER.error("Dialect \"{}\" intersects with previously declared \"{}\", dialect. Preferring new", dialect.name(), dialect.name());
        }
        DIALECTS.put(dialect.name(), dialect);
    }

    public static @NotNull Optional<DialogueDialect> get(String dialectName) {
        return Optional.ofNullable(DIALECTS.get(dialectName));
    }

    public static @NotNull @UnmodifiableView Collection<DialogueDialect> getDialects() {
        return Collections.unmodifiableCollection(DIALECTS.values());
    }
}
