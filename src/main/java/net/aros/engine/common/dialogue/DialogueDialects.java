package net.aros.engine.common.dialogue;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.events.RegisterDialogueDialectEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.*;

public class DialogueDialects {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, DialogueDialect> DIALECTS = new HashMap<>();

    public static @NotNull Optional<DialogueDialect> get(String dialectName) {
        return Optional.ofNullable(DIALECTS.get(dialectName));
    }

    public static @NotNull @UnmodifiableView Collection<DialogueDialect> getDialects() {
        return Collections.unmodifiableCollection(DIALECTS.values());
    }

    public static void reload() {
        DIALECTS.clear();
        DIALECTS.putAll(ModLoader.postEventWithReturn(new RegisterDialogueDialectEvent()).dialects());
    }
}
