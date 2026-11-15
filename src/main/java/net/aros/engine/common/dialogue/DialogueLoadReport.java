package net.aros.engine.common.dialogue;

import net.aros.engine.common.ArosEngine;
import net.aros.haru.diagnostic.Diagnostic;
import net.aros.haru.diagnostic.DiagnosticRenderer;
import net.aros.haru.diagnostic.Severity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Immutable snapshot; diagnostics keep their source text even after files are edited. */
public record DialogueLoadReport(String operation, int loaded, List<Entry> entries,
                                 Map<ResourceLocation, String> origins) {
    public DialogueLoadReport {
        entries = List.copyOf(entries);
        origins = Map.copyOf(origins);
    }

    public record Entry(Severity severity, String text) {
        public static Entry diagnostic(Diagnostic diagnostic, Path path, String source) {
            String rendered = DiagnosticRenderer.render(List.of(diagnostic), path, source)
                    .replaceAll("\u001B\\[[0-9;]*m", "").stripTrailing();
            return new Entry(diagnostic.severity(), diagnostic.span() == null ? path + ":\n" + rendered : rendered);
        }
    }

    public long errors() {
        return entries.stream().filter(entry -> entry.severity() == Severity.ERROR).count();
    }

    public long warnings() {
        return entries.stream().filter(entry -> entry.severity() == Severity.WARNING).count();
    }

    public static Component statusLink() {
        return ArosEngine.translation("commands", "dialogue.status.link").withStyle(style -> style
                .withColor(ChatFormatting.AQUA).withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/aengine dialogue status")));
    }

    public void display(CommandSourceStack source) {
        source.sendSystemMessage(ArosEngine.translation("commands", "dialogue.status.header",
                operation, loaded, errors(), warnings()).withStyle(ChatFormatting.GOLD));
        for (Entry entry : entries) {
            ChatFormatting color = switch (entry.severity()) {
                case ERROR -> ChatFormatting.RED;
                case WARNING -> ChatFormatting.YELLOW;
                case HINT -> ChatFormatting.AQUA;
            };
            for (String line : entry.text().split("\\R")) {
                source.sendSystemMessage(Component.literal(line).withStyle(color));
            }
        }
    }
}
