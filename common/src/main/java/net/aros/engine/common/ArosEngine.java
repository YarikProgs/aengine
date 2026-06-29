package net.aros.engine.common;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.init.AEngineRegistries;
import net.aros.engine.common.init.AEngineValueTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class ArosEngine {
    public static final String MOD_ID = "aengine";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static CommonEngine engine;

    public static void init() {
        LOGGER.info("Aros' Engine is initializing...");

        AEngineRegistries.touch();
        AEngineValueTypes.touch();

        engine = new CommonEngine();
        engine.initialize();
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static CommonEngine getEngine() {
        if (engine == null) throw new IllegalStateException("getEngine call too early");
        return engine;
    }
}
