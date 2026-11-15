package net.aros.engine.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.Dialogues;
import net.aros.engine.common.dialogue.DialogueProjectExporter;
import net.aros.engine.common.dialogue.DialogueReloadListener;
import net.aros.engine.common.dialogue.playing.DialogueDebug;
import net.aros.engine.common.dialogue.playing.DialoguesTicker;
import net.aros.engine.common.dialogue.playing.DialogueInstance;
import net.aros.engine.common.dialogue.playing.EndReason;
import net.aros.engine.common.dialogue.playing.PauseReason;
import net.aros.engine.common.dialogue.playing.SoloAudience;
import net.aros.engine.common.util.AeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class AEngineCommand {
    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = literal(ArosEngine.MOD_ID);
        Dialogue.register(root);
        dispatcher.register(root);
    }

    private static class Dialogue {
        public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
            root.then(literal("dialogue")
                    .requires(source -> source.hasPermission(2))
                    .then(literal("status").executes(ctx -> status(ctx.getSource())))
                    .then(literal("debug")
                            .then(literal("on").executes(ctx -> debug(ctx.getSource(), true)))
                            .then(literal("off").executes(ctx -> debug(ctx.getSource(), false))))
                    .then(literal("info")
                            .executes(ctx -> infoCurrent(ctx.getSource()))
                            .then(argument("id", ResourceLocationArgument.id())
                                    .suggests(Dialogue::suggestDialogueId)
                                    .executes(ctx -> info(ctx.getSource(), ResourceLocationArgument.getId(ctx, "id")))))
                    .then(literal("list")
                            .executes(ctx -> list(ctx.getSource())))
                    .then(literal("play")
                            .then(argument("id", ResourceLocationArgument.id())
                                    .suggests(Dialogue::suggestDialogueId)
                                    .executes(ctx -> play(ctx.getSource(), ResourceLocationArgument.getId(ctx, "id")))))
                    .then(literal("pause")
                            .executes(ctx -> pause(ctx.getSource())))
                    .then(literal("resume")
                            .executes(ctx -> resume(ctx.getSource())))
                    .then(literal("cancel")
                            .executes(ctx -> cancel(ctx.getSource())))
                    .then(literal("project")
                            .then(literal("list")
                                    .executes(ctx -> projectList(ctx.getSource())))
                            .then(literal("create")
                                    .then(argument("name", StringArgumentType.word())
                                            .executes(ctx -> projectCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                            .then(literal("open")
                                    .then(argument("name", StringArgumentType.word())
                                            .suggests(Dialogue::suggestProjectName)
                                            .executes(ctx -> projectOpen(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                            .then(literal("export")
                                    .then(argument("name", StringArgumentType.word())
                                            .suggests(Dialogue::suggestProjectName)
                                            .executes(ctx -> projectExport(ctx.getSource(), StringArgumentType.getString(ctx, "name"), true, false))
                                            .then(literal("overwrite").executes(ctx -> projectExport(ctx.getSource(), StringArgumentType.getString(ctx, "name"), true, true)))))
                            .then(literal("export-mod")
                                    .then(argument("name", StringArgumentType.word())
                                            .suggests(Dialogue::suggestProjectName)
                                            .executes(ctx -> projectExport(ctx.getSource(), StringArgumentType.getString(ctx, "name"), false, false))
                                            .then(literal("overwrite").executes(ctx -> projectExport(ctx.getSource(), StringArgumentType.getString(ctx, "name"), false, true)))))));
        }

        private static int status(CommandSourceStack source) {
            var report = DialogueReloadListener.lastReport();
            if (report == null) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue.status.none"));
                return 0;
            }
            report.display(source);
            return Command.SINGLE_SUCCESS;
        }

        private static int debug(CommandSourceStack source, boolean enabled) throws CommandSyntaxException {
            DialogueDebug.set(source.getPlayerOrException(), enabled);
            source.sendSuccess(() -> ArosEngine.translation("commands", enabled ? "dialogue.debug.on" : "dialogue.debug.off"), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int infoCurrent(CommandSourceStack source) throws CommandSyntaxException {
            var active = DialoguesTicker.activeFor(source.getPlayerOrException()).orElse(null);
            if (active == null) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue.info.none"));
                return 0;
            }
            displayInfo(source, active.dialogue());
            source.sendSystemMessage(ArosEngine.translation("commands", "dialogue.info.instance",
                    active.instanceId().toString(), active.debugState(), active.currentMembers().size(), active.audience().admin().getGameProfile().getName())
                    .withStyle(ChatFormatting.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        private static int info(CommandSourceStack source, ResourceLocation id) {
            var dialogue = Dialogues.get(id).orElse(null);
            if (dialogue == null) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue.play.not_found", id.toString()));
                return 0;
            }
            displayInfo(source, dialogue);
            return Command.SINGLE_SUCCESS;
        }

        private static void displayInfo(CommandSourceStack source, net.aros.engine.common.dialogue.Dialogue dialogue) {
            var report = DialogueReloadListener.lastReport();
            String origin = report == null ? "?" : report.origins().getOrDefault(dialogue.id(), "?");
            source.sendSystemMessage(ArosEngine.translation("commands", "dialogue.info.header", dialogue.id().toString()).withStyle(ChatFormatting.GOLD));
            source.sendSystemMessage(ArosEngine.translation("commands", "dialogue.info.details",
                    dialogue.dialect().name(), dialogue.metadata().priority(), dialogue.metadata().onAudienceChange().name(), origin)
                    .withStyle(ChatFormatting.GRAY));
        }

        private static int play(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
            if (Dialogues.get(id).isEmpty()) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue.play.not_found", id.toString()));
                return 0;
            }

            var player = source.getPlayerOrException();
            var result = DialoguesTicker.start(new SoloAudience(player), id, Map.of());
            return switch (result) {
                case STARTED -> {
                    source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.play.started", id.toString()), false);
                    yield Command.SINGLE_SUCCESS;
                }
                case QUEUED -> {
                    source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.play.queued", id.toString()), false);
                    yield Command.SINGLE_SUCCESS;
                }
                case REJECTED -> {
                    source.sendFailure(ArosEngine.translation("commands", "dialogue.play.rejected", id.toString()));
                    yield 0;
                }
            };
        }

        private static int pause(CommandSourceStack source) throws CommandSyntaxException {
            var player = source.getPlayerOrException();
            var active = requireAdminDialogue(source, player, "pause");
            if (active == null) return 0;
            active.pause(PauseReason.ADMIN);
            source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.pause.success"), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int resume(CommandSourceStack source) throws CommandSyntaxException {
            var player = source.getPlayerOrException();
            var active = requireAdminDialogue(source, player, "resume");
            if (active == null) return 0;
            active.resume(PauseReason.ADMIN);
            source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.resume.success"), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int cancel(CommandSourceStack source) throws CommandSyntaxException {
            var player = source.getPlayerOrException();
            var active = requireAdminDialogue(source, player, "cancel");
            if (active == null) return 0;
            active.finish(EndReason.CANCELLED);
            source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.cancel.success"), false);
            return Command.SINGLE_SUCCESS;
        }

        /** Returns the active dialogue after checking the admin, or sends failure feedback. */
        private static @Nullable DialogueInstance requireAdminDialogue(CommandSourceStack source, ServerPlayer player, String action) {
            var active = DialoguesTicker.activeFor(player).orElse(null);
            if (active == null) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue." + action + ".none"));
                return null;
            }
            if (!active.isAdmin(player)) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue." + action + ".not_admin"));
                return null;
            }
            return active;
        }

        private static CompletableFuture<Suggestions> suggestDialogueId(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
            return SharedSuggestionProvider.suggestResource(Dialogues.getAll().keySet(), builder);
        }

        private static int list(CommandSourceStack source) {
            var dialogues = Dialogues.getAll();
            if (dialogues.isEmpty()) {
                source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.list.empty"), false);
                return Command.SINGLE_SUCCESS;
            }

            Map<String, List<ResourceLocation>> grouped = dialogues.keySet().stream()
                    .collect(Collectors.groupingBy(ResourceLocation::getNamespace, TreeMap::new, Collectors.toList()));
            grouped.forEach((modId, ids) -> {
                var entries = ids.stream()
                        .sorted()
                        .map(id -> Component.literal(id.getPath())
                                .withStyle(ChatFormatting.GRAY))
                        .toList();
                source.sendSystemMessage(ArosEngine.translation("commands", "dialogue.list.namespace", modId));
                entries.forEach(entry -> source.sendSystemMessage(
                        ArosEngine.translation("commands", "dialogue.list.entry", entry)));
            });
            return dialogues.size();
        }

        private static int projectCreate(CommandSourceStack source, String name) {
            if (!checkModId(source, name)) return 0;

            Path dir = projectDir(name);
            if (Files.exists(dir)) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue.project.new.already_exists", name));
                return 0;
            }

            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                ArosEngine.LOGGER.error("Failed to create dialogue project '{}'", name, e);
                source.sendFailure(ArosEngine.translation("commands", "dialogue.project.new.failed", name));
                return 0;
            }

            source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.project.new.success", name, openLink(dir)), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int projectOpen(CommandSourceStack source, String name) {
            if (!checkModId(source, name)) return 0;

            Path dir = projectDir(name);
            if (Files.notExists(dir)) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue.project.open.not_found", name));
                return 0;
            }

            source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.project.open.success", name, openLink(dir)), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int projectExport(CommandSourceStack source, String name, boolean archive, boolean overwrite) {
            if (!checkModId(source, name)) return 0;

            Path project = projectDir(name);
            if (!Files.isDirectory(project, LinkOption.NOFOLLOW_LINKS)) {
                source.sendFailure(ArosEngine.translation("commands", "dialogue.project.open.not_found", name));
                return 0;
            }
            Path target = AeUtil.getCacheDir().resolve(archive ? "exports" : "mod-exports")
                    .resolve(archive ? name + ".zip" : name);
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS) && !overwrite) {
                String command = "/aengine dialogue project " + (archive ? "export " : "export-mod ") + name + " overwrite";
                var confirmation = Component.literal(command).withStyle(style -> style.withColor(ChatFormatting.YELLOW)
                        .withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));
                source.sendSystemMessage(ArosEngine.translation("commands", "dialogue.project.export.already_exists", openLink(target), confirmation)
                        .withStyle(ChatFormatting.YELLOW));
                return 0;
            }
            try {
                var report = DialogueReloadListener.compileProject(source.getServer().getResourceManager(), name);
                if (report.errors() > 0) {
                    source.sendFailure(ArosEngine.translation("commands", "dialogue.project.export.compile_failed", name, report.errors()));
                    report.display(source);
                    return 0;
                }
                if (report.warnings() > 0) report.display(source);
                DialogueProjectExporter.export(project, target, name, archive, overwrite);
            } catch (IOException e) {
                ArosEngine.LOGGER.error("Failed to export dialogue project '{}' to '{}'", name, target, e);
                source.sendFailure(ArosEngine.translation("commands", "dialogue.project.export.failed", name));
                return 0;
            }
            source.sendSuccess(() -> ArosEngine.translation("commands",
                    archive ? "dialogue.project.export.success" : "dialogue.project.export_mod.success",
                    name, openLink(target)), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int projectList(CommandSourceStack source) {
            try (Stream<Path> projects = Files.list(projectsDir())) {
                List<Path> dirs = projects
                        .filter(Files::isDirectory)
                        .filter(p -> ResourceLocation.isValidNamespace(p.getFileName().toString()))
                        .sorted()
                        .toList();
                if (dirs.isEmpty()) {
                    source.sendSuccess(() -> ArosEngine.translation("commands", "dialogue.project.list.empty"), false);
                    return Command.SINGLE_SUCCESS;
                }

                for (Path dir : dirs) {
                    long dialogues = countDialogues(dir);
                    source.sendSystemMessage(ArosEngine.translation(
                            "commands", "dialogue.project.list.entry",
                            Component.literal(dir.getFileName().toString()).withStyle(ChatFormatting.WHITE),
                            dialogues));
                }
                return dirs.size();
            } catch (IOException e) {
                ArosEngine.LOGGER.error("Failed to list dialogue projects", e);
                source.sendFailure(ArosEngine.translation("commands", "dialogue.project.list.failed"));
                return 0;
            }
        }

        private static long countDialogues(Path projectDir) {
            try (Stream<Path> files = Files.walk(projectDir)) {
                return files.filter(Files::isRegularFile)
                        .filter(f -> f.getFileName().toString().endsWith(".haru"))
                        .count();
            } catch (IOException e) {
                return 0;
            }
        }

        private static boolean checkModId(CommandSourceStack source, String name) {
            if (ResourceLocation.isValidNamespace(name) && !name.equals(".") && !name.equals("..")) return true;
            source.sendFailure(ArosEngine.translation("commands", "dialogue.project.invalid_name", name));
            return false;
        }

        private static Path projectDir(String name) {
            return projectsDir().resolve(name);
        }

        private static Path projectsDir() {
            return AeUtil.getCacheDir().resolve("dialogue").resolve("projects");
        }

        private static CompletableFuture<Suggestions> suggestProjectName(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
            try (Stream<Path> projects = Files.list(projectsDir())) {
                return SharedSuggestionProvider.suggest(
                        projects.filter(Files::isDirectory)
                                .map(p -> p.getFileName().toString())
                                .filter(ResourceLocation::isValidNamespace)
                                .sorted(),
                        builder);
            } catch (IOException e) {
                return builder.buildFuture();
            }
        }

        private static Component openLink(Path dir) {
            return ComponentUtils
                    .wrapInSquareBrackets(ArosEngine.translation("commands", "dialogue.project.open_link"))
                    .withStyle(style -> style
                            .withColor(ChatFormatting.GREEN)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, dir.toAbsolutePath().toString())));
        }
    }
}
