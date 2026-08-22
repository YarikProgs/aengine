package net.aros.engine.common.api.script.callables.commandcodecs;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface CommandCodec<T> {
    <S extends ArgumentBuilder<CommandSourceStack, S>>
    ArgumentBuilder<CommandSourceStack, S> addToBuilder(ArgumentBuilder<CommandSourceStack, S> builder);

    T get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;

    static CommandCodec<MinecraftServer> unit() {
        return new CommandCodec<>() {
            @Override
            public <S extends ArgumentBuilder<CommandSourceStack, S>> ArgumentBuilder<CommandSourceStack, S> addToBuilder(ArgumentBuilder<CommandSourceStack, S> builder) {
                return builder;
            }

            @Override
            public MinecraftServer get(CommandContext<CommandSourceStack> context) {
                return context.getSource().getServer();
            }
        };
    }

    default <E> CommandCodec<E> map(Function<T, E> to) {
        return new CommandCodec<>() {
            @Override
            public <S extends ArgumentBuilder<CommandSourceStack, S>> ArgumentBuilder<CommandSourceStack, S> addToBuilder(ArgumentBuilder<CommandSourceStack, S> builder) {
                return CommandCodec.this.addToBuilder(builder);
            }

            @Override
            public E get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                return to.apply(CommandCodec.this.get(context));
            }
        };
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    static <T, A extends ArgumentType<?>> @NotNull CommandCodec<T> ofPrimitive(
            A argumentType,
            ArgumentGetter<T> getter,
            String name
    ) {
        return new CommandCodec<>() {
            @Override
            public <S extends ArgumentBuilder<CommandSourceStack, S>>
            ArgumentBuilder<CommandSourceStack, S> addToBuilder(ArgumentBuilder<CommandSourceStack, S> builder) {
                builder.then(Commands.argument(name, (ArgumentType<?>) argumentType));
                return builder;
            }

            @Override
            public T get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                return getter.get(context, name);
            }
        };
    }

    @FunctionalInterface
    interface ArgumentGetter<T> {
        T get(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException;
    }
}