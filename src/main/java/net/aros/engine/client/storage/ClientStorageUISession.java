package net.aros.engine.client.storage;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import net.aros.engine.client.ArosEngineClient;
import net.aros.engine.client.api.ClientStorageManager;
import net.aros.engine.client.gui.ui.StorageUI;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.aros.engine.common.storage.ContentUpdate;
import net.aros.engine.common.storage.InitialData;
import net.minecraft.client.Minecraft;

import java.util.Optional;

public class ClientStorageUISession {
    @RPCPacket("session.storageui.receive_metadata")
    public static void receiveMetadata(StorageKey<?> key, StorageMetadata metadata) {
        getCurrentStorageUI().ifPresent(ui -> ui.putMetadata(key, metadata, false));
    }

    @RPCPacket("session.storageui.receive_initial_data")
    public static void receiveInitialInfo(InitialData initialData) {
        getCurrentStorageUI().ifPresent(ui -> {
            ui.putAllMetadata(initialData.metadataMap(), false);
            ui.putAllContents(initialData.contentsList(), false);
        });
    }

    @RPCPacket("session.storageui.receive_content")
    public static <T> void receiveContentUpdate(ContentUpdate<T> update) {
        getCurrentStorageUI().ifPresent(ui -> ui.putContents(update, false));
    }

    public static void createBoth() {
        RPCPacketDistributor.rpcToServer("session.storageui.create");
        ClientStorageManager.sessionManager().create(null);
    }

    public static void closeBoth() {
        RPCPacketDistributor.rpcToServer("session.storageui.close");
        ClientStorageManager.sessionManager().close(null);
    }

    private static Optional<StorageUI> getCurrentStorageUI() {
        if (!(Minecraft.getInstance().screen instanceof ModularUIScreen modularUIScreen)) return Optional.empty();
        if (!(modularUIScreen.modularUI.ui.rootElement instanceof StorageUI storageUI)) return Optional.empty();
        return Optional.of(storageUI);
    }
}
