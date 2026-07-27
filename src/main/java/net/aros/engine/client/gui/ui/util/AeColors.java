package net.aros.engine.client.gui.ui.util;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.metadata.LoadStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class AeColors {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FALLBACK_COLOR = 0xFF000000;

    public static final int SOFT_GREEN_COLOR = parseColor("Soft Green", ArosEngine.CONFIG.colors.softGreen());
    public static final int SOFT_YELLOW_COLOR = parseColor("Soft Yellow", ArosEngine.CONFIG.colors.softYellow());
    public static final int SOFT_RED_COLOR = parseColor("Soft Red", ArosEngine.CONFIG.colors.softRed());

    @Contract(pure = true)
    public static int getStatusColor(@NotNull LoadStatus status) {
        return switch (status) {
            case OK -> SOFT_GREEN_COLOR;
            case RELOADING -> 0xFFFFFFFF;
            case FAIL -> SOFT_RED_COLOR;
        };
    }

    public static int getLoadTimeColor(long loadTimeMs) {
        if (loadTimeMs == -1)
            return 0xFFaaaaaa;
        if (loadTimeMs < ArosEngine.CONFIG.storageUI.okTimeThresholdMs())
            return SOFT_GREEN_COLOR;
        if (loadTimeMs < ArosEngine.CONFIG.storageUI.warnTimeThresholdMs())
            return SOFT_YELLOW_COLOR;
        return SOFT_RED_COLOR;
    }

    private static int parseColor(String name, String color) {
        if (color == null) {
            LOGGER.warn("'{}' color value is null, consider checking client config", name);
            return FALLBACK_COLOR;
        }
        try {
            if (color.startsWith("#")) {
                return Integer.parseUnsignedInt(color.toLowerCase().substring(1), 16);
            }
            return Integer.parseUnsignedInt(color);
        } catch (NumberFormatException e) {
            LOGGER.warn("'{}' color value is invalid, consider checking client config." +
                    "Allowed formats are: hex (such as #FFAABBCC), decimal (such as -5588020)", name);
            return FALLBACK_COLOR;
        }
    }
}
