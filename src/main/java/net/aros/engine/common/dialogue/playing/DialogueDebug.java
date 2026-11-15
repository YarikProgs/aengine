package net.aros.engine.common.dialogue.playing;

import net.aros.engine.common.ArosEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;

/** Session-local, per-player tracing; permissions are checked again on every delivery. */
public final class DialogueDebug {
    private static final Set<UUID> ENABLED = new HashSet<>();

    private DialogueDebug() {}

    public static void set(ServerPlayer player, boolean enabled) {
        if (enabled) ENABLED.add(player.getUUID());
        else ENABLED.remove(player.getUUID());
    }

    public static void clear() {
        ENABLED.clear();
    }

    public static void send(ServerPlayer player, ResourceLocation id, String event, Object... args) {
        if (!ENABLED.contains(player.getUUID()) || player.hasDisconnected()
                || !player.createCommandSourceStack().hasPermission(2)) return;
        Object[] values = Arrays.stream(args).map(value -> value instanceof Number || value instanceof Boolean
                || value instanceof net.minecraft.network.chat.Component ? value : String.valueOf(value)).toArray();
        player.sendSystemMessage(ArosEngine.translation("commands", "dialogue.debug.entry", id.toString(),
                ArosEngine.translation("commands", "dialogue.debug." + event, values)).withStyle(ChatFormatting.DARK_AQUA));
    }
}
