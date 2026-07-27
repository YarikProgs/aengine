package net.aros.engine.common;

import net.aros.engine.common.api.storage.AbstractStorage;
import net.aros.engine.common.api.value.ValueAccess;
import net.aros.engine.common.storage.ServerStorageUISessionManager;
import net.aros.engine.common.storage.StorageManager;
import net.aros.engine.common.systems.CustomStorageSystem;
import net.aros.engine.common.systems.ServerTickSystem;
import net.aros.engine.server.ValuesSavedData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommonEngine {
    private final StorageManager storageManager = new StorageManager(new ServerStorageUISessionManager());
    private final List<ServerTickSystem> tickSystems = new ArrayList<>();
    private ValueAccess valueAccess;

    public void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::serverTick);
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
    }

    public void register(Object system) {
        if (system instanceof ServerTickSystem tickSystem) {
            tickSystems.add(tickSystem);
        }
        if (system instanceof CustomStorageSystem customStorageSystem) {
            registerStoragesFrom(customStorageSystem);
        }
    }

    private void serverStarted(MinecraftServer server) {
        valueAccess = ValueAccess.wrap(ValuesSavedData.of(server));
    }

    public void serverTick(MinecraftServer server) {
        var ctx = new ServerTickSystem.Context(server, valueAccess, storageManager().storageAccess());
        for (ServerTickSystem tickSystem : tickSystems) {
            tickSystem.tick(ctx);
        }
    }

    private void registerStoragesFrom(@NotNull CustomStorageSystem customStorageSystem) {
        var ctx = new CustomStorageSystem.Context();
        customStorageSystem.onStorageRegistration(ctx);

        storageManager().putAll(ctx.getRegisteredStorages());
        for (AbstractStorage<?> storage : ctx.getRegisteredStorages().values()) {
            storage.setManager(storageManager);
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(storage.createReloadListener());
        }
    }

    public ValueAccess values() {
        if (valueAccess == null) throw new IllegalStateException("Called values too early");
        return valueAccess;
    }

    public StorageManager storageManager() {
        return storageManager;
    }
}
