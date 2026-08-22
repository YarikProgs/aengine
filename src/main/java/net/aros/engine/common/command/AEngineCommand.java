package net.aros.engine.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.aros.engine.common.ArosEngine;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public final class AEngineCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = literal(ArosEngine.MOD_ID);

//        Scriptal.register(root);

        dispatcher.register(root);
    }
}
