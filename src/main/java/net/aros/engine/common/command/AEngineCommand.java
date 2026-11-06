package net.aros.engine.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.Dialogues;
import net.aros.engine.common.util.AeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
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
                    .then(literal("list")
                            .executes(ctx -> list(ctx.getSource())))
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
                                            .executes(ctx -> projectExport(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))));
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

        private static int projectExport(CommandSourceStack source, String name) {
            if (!checkModId(source, name)) return 0;

            // TODO: export logic
            throw new IllegalStateException("Not implemented yet");
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
            if (ResourceLocation.isValidNamespace(name)) return true;
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
