package net.aros.engine.client.systems;

import net.aros.engine.common.api.storage.StorageAccess;
import net.minecraft.client.Minecraft;

public interface ClientTickSystem {
    void tick(Context ctx);

    record Context(Minecraft mc, StorageAccess access) {
    }
}
