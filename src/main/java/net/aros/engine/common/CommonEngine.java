package net.aros.engine.common;

import net.aros.engine.common.storage.storage.AbstractStorage;
import net.aros.engine.common.storage.storage.StorageAccess;
import net.aros.engine.common.storage.storage.StorageKey;
import net.aros.engine.common.storage.value.ValueAccess;
import net.aros.engine.common.systems.StorageSystem;
import net.aros.engine.common.systems.ServerTickSystem;
import net.aros.engine.server.StorageSavedData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonEngine {
    private final List<ServerTickSystem> tickSystems = new ArrayList<>();

    private final Map<StorageKey<?>, AbstractStorage<?>> storages = new HashMap<>();
    private final StorageAccess storageAccess = StorageAccess.wrap(storages);
    private ValueAccess valueAccess;

    public void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::serverTick);
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
    }

    public void register(Object system) {
        if (system instanceof ServerTickSystem tickSystem) {
            tickSystems.add(tickSystem);
        }
        if (system instanceof StorageSystem storageSystem) {
            registerStoragesFrom(storageSystem);
        }
    }

    private void serverStarted(MinecraftServer server) {
        valueAccess = ValueAccess.wrap(StorageSavedData.of(server));
    }

    public void serverTick(MinecraftServer server) {
        var ctx = new ServerTickSystem.Context(server, valueAccess, storageAccess);
        for (ServerTickSystem tickSystem : tickSystems) {
            tickSystem.tick(ctx);
        }
    }

    private void registerStoragesFrom(@NotNull StorageSystem storageSystem) {
        var ctx = new StorageSystem.Context();
        storageSystem.onStorageRegistration(ctx);

        storages.putAll(ctx.getRegisteredStorages());
        for (AbstractStorage<?> storage : ctx.getRegisteredStorages().values()) {
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(storage.getReloadListener());
        }
    }

    public ValueAccess values() {
        if (valueAccess == null) throw new IllegalStateException("Called values too early");
        return valueAccess;
    }
}
