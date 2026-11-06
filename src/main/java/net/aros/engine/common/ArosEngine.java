package net.aros.engine.common;

import com.google.common.base.Predicates;
import com.mojang.logging.LogUtils;
import net.aros.engine.client.ArosEngineClient;
import net.aros.engine.common.command.AEngineCommand;
import net.aros.engine.common.config.AeConfig;
import net.aros.engine.common.dialogue.DialogueReloadListener;
import net.aros.engine.common.init.AeLdlibTypes;
import net.aros.engine.common.init.AeRegistries;
import net.aros.engine.common.init.AeValueTypes;
import net.aros.engine.common.network.s2c.ProcedureCallPayload;
import net.aros.engine.common.script.KotlinScriptBackend;
import net.aros.engine.common.script.ScriptBackends;
import net.aros.engine.common.script.ScriptReloadListener;
import net.aros.engine.common.script.events.EventSubscriptions;
import net.aros.engine.common.script.procedure.Procedures;
import net.aros.examples.common.ArosEngineExamples;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod(ArosEngine.MOD_ID)
public final class ArosEngine {
    public static final String MOD_ID = "aengine";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final AeConfig CONFIG = AeConfig.createAndLoad();

    public ArosEngine(IEventBus bus) {
        LOGGER.info("Aros' Engine is initializing...");

        AeRegistries.touch();
        AeValueTypes.touch();
        AeLdlibTypes.init();

        ScriptBackends.register(KotlinScriptBackend.INSTANCE);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e
                -> AEngineCommand.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, e -> {
            e.addListener(new ScriptReloadListener(
                    () -> {
                        Procedures.clear();
                        EventSubscriptions.clear();
                    }, Predicates::alwaysTrue));
            e.addListener(new DialogueReloadListener());
        });
        bus.addListener(RegisterPayloadHandlersEvent.class, e
                -> e.registrar("1.0.0").playToClient(ProcedureCallPayload.TYPE, ProcedureCallPayload.STREAM_CODEC, ArosEngineClient::handleProcedureCall));

        if (CONFIG.debug.examplesEnabled()) {
            new ArosEngineExamples(bus);
        }
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Contract(pure = true)
    public static @NotNull String translationKey(@Nullable String prefix, @Nullable String postfix) {
        return "%s%s%s".formatted(prefix == null ? "" : prefix + '.', MOD_ID, postfix == null ? "" : '.' + postfix);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull MutableComponent translation(@Nullable String prefix, @Nullable String postfix, Object... args) {
        return Component.translatable(translationKey(prefix, postfix), args);
    }
}
