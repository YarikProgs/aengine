package net.aros.engine;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class ArosEngine {
    public static final String MOD_ID = "aengine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        LOGGER.info("Aros' Engine is initializing...");
    }
}
