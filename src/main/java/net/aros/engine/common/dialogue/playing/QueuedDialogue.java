package net.aros.engine.common.dialogue.playing;

import net.aros.engine.common.dialogue.Dialogue;
import net.aros.haru.value.Value;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A dialogue waiting in an audience queue (ТЗ п. 6).
 */
public record QueuedDialogue(
        Dialogue dialogue,
        Map<String, Value> externals
) {
}
