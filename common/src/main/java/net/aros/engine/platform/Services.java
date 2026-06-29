package net.aros.engine.platform;

import net.aros.engine.platform.services.PlatformHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

public class Services {
    public static final PlatformHelper PLATFORM = load(PlatformHelper.class);

    public static <T> @NotNull T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst().orElseThrow(()
                -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
