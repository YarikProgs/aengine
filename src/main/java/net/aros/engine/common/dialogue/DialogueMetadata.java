package net.aros.engine.common.dialogue;

import org.jetbrains.annotations.NotNull;

/**
 * Static properties of a dialogue, read from the compiled haru exports at load
 * time without executing the script.
 *
 * @param priority         static priority, bigger wins, preempts lower
 * @param onAudienceChange policy applied when the audience composition changes
 */
public record DialogueMetadata(int priority, OnAudienceChange onAudienceChange) {
    public static final int DEFAULT_PRIORITY = 0;

    public static @NotNull DialogueMetadata defaults(OnAudienceChange defaultPolicy) {
        return new DialogueMetadata(DEFAULT_PRIORITY, defaultPolicy);
    }

    public boolean preempts(@NotNull DialogueMetadata other) {
        return priority > other.priority;
    }
}
