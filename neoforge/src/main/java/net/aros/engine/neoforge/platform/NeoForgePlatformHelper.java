package net.aros.engine.neoforge.platform;

import net.aros.engine.platform.services.PlatformHelper;
import net.neoforged.fml.ModList;

public class NeoForgePlatformHelper implements PlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
