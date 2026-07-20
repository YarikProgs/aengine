package net.aros.engine.client.gui.util;

import net.aros.engine.common.api.storage.metadata.LoadStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class AeColors {
    // TODO: 08.07.2026 config
    public static final int OK_TIME_THRESHOLD_MS = 2000;
    public static final int WARN_TIME_THRESHOLD_MS = 10000;

    public static final int SOFT_GREEN_COLOR = 0xFFa8fca4;
    public static final int SOFT_YELLOW_COLOR = 0xFFf3fca4;
    public static final int SOFT_RED_COLOR = 0xFFfca4a4;

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
        if (loadTimeMs < OK_TIME_THRESHOLD_MS)
            return SOFT_GREEN_COLOR;
        if (loadTimeMs < WARN_TIME_THRESHOLD_MS)
            return SOFT_YELLOW_COLOR;
        return SOFT_RED_COLOR;
    }
}
