package net.aros.engine.common.internal.systems;

import net.aros.engine.common.api.storage.storage.StorageAccess;
import net.aros.engine.common.api.storage.value.ValueAccess;
import net.minecraft.server.MinecraftServer;

public interface ServerTickSystem {
    void tick(Context ctx);

    record Context(MinecraftServer server, ValueAccess valueAccess, StorageAccess storageAccess) {
    }
}
