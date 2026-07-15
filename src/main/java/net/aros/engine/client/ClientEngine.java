package net.aros.engine.client;

import net.aros.engine.client.api.ui.ModuleUIProvider;
import net.aros.engine.client.storage.ClientStorageUISessionManager;
import net.aros.engine.client.systems.ClientTickSystem;
import net.aros.engine.client.systems.ModuleUISystem;
import net.aros.engine.common.api.storage.storage.AbstractStorage;
import net.aros.engine.common.storage.ServerStorageUISessionManager;
import net.aros.engine.common.storage.StorageManager;
import net.aros.engine.common.systems.CustomStorageSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClientEngine {
    private final List<ClientTickSystem> tickSystems = new ArrayList<>();
    private final Map<ResourceLocation, ModuleUIProvider> uiProviders = new HashMap<>();

    private final StorageManager storageManager = new StorageManager(new ClientStorageUISessionManager());

    public void initializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::clientTick);
    }

    public void register(Object system) {
        if (system instanceof ClientTickSystem tickSystem) {
            tickSystems.add(tickSystem);
        }
        if (system instanceof CustomStorageSystem customStorageSystem) {
            registerStoragesFrom(customStorageSystem);
        }
        if (system instanceof ModuleUISystem moduleUiSystem) {
            registerUiProviders(moduleUiSystem);
        }
    }

    public void clientTick(Minecraft mc) {
        for (ClientTickSystem tickSystem : tickSystems) {
            tickSystem.tick(new ClientTickSystem.Context(mc, storageManager().storageAccess()));
        }
    }

    private void registerStoragesFrom(@NotNull CustomStorageSystem customStorageSystem) {
        var ctx = new CustomStorageSystem.Context();
        customStorageSystem.onStorageRegistration(ctx);

        storageManager().putAll(ctx.getRegisteredStorages());
        for (AbstractStorage<?> storage : ctx.getRegisteredStorages().values()) {
            storage.setManager(storageManager);
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(storage.createReloadListener());
        }
    }

    private void registerUiProviders(@NotNull ModuleUISystem moduleUiSystem) {
        var ctx = new ModuleUISystem.Context();
        moduleUiSystem.onUIRegister(ctx);
        uiProviders.putAll(ctx.getProviders());
    }

    public Map<ResourceLocation, ModuleUIProvider> getUiProviders() {
        return Collections.unmodifiableMap(uiProviders);
    }

    public StorageManager storageManager() {
        return storageManager;
    }
}
