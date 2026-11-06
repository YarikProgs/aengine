package net.aros.engine.common.dialogue;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Dialogues {
    private static final Map<ResourceLocation, Dialogue> DIALOGUES = new HashMap<>();

    public static @NotNull Optional<Dialogue> get(ResourceLocation key) {
        return Optional.ofNullable(DIALOGUES.get(key));
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView Map<ResourceLocation, Dialogue> getAll() {
        return Collections.unmodifiableMap(DIALOGUES);
    }

    static void replace(Map<ResourceLocation, Dialogue> newDialogues) {
        DIALOGUES.clear();
        DIALOGUES.putAll(newDialogues);
    }
}
