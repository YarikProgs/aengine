package net.aros.engine.common.systems;

import net.aros.engine.common.storage.storage.StorageAccess;
import net.aros.engine.common.storage.value.ValueAccess;
import net.minecraft.server.MinecraftServer;

public interface ServerTickSystem {
    void tick(Context ctx);

    record Context(MinecraftServer server, ValueAccess valueAccess, StorageAccess storageAccess) {
    }
}
