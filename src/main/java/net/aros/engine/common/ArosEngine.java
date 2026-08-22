package net.aros.engine.common;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.api.script.AeScriptApi;
import net.aros.engine.common.command.AEngineCommand;
import net.aros.engine.common.config.AeConfig;
import net.aros.engine.common.init.AeRegistries;
import net.aros.engine.common.init.AeValueTypes;
import net.aros.engine.common.script.AeScriptSystem;
import net.aros.examples.common.ArosEngineExamples;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod(ArosEngine.MOD_ID)
public final class ArosEngine {
    public static final String MOD_ID = "aengine";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final AeConfig CONFIG = AeConfig.createAndLoad();

    private static CommonEngine engine;

    public ArosEngine() {
        LOGGER.info("Aros' Engine is initializing...");

        AeRegistries.touch();
        AeValueTypes.touch();

        engine = new CommonEngine();
        engine.initialize();
        engine.register(new AeScriptSystem());

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> AEngineCommand.register(e.getDispatcher()));
        AeScriptApi.initialize();

        if (CONFIG.debug.examplesEnabled()) {
            new ArosEngineExamples();
        }
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static CommonEngine getEngine() {
        if (engine == null) throw new IllegalStateException("getEngine call too early");
        return engine;
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
