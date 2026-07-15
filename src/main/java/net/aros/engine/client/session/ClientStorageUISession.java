package net.aros.engine.client.session;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.aros.engine.client.ArosEngineClient;
import net.aros.engine.client.gui.ui.StorageUI;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadataMap;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.Optional;

public class ClientStorageUISession {
    @RPCPacket("session.storageui.receive_metadata")
    public static void receiveMetadata(StorageKey<?> key, StorageMetadata metadata) {
        getCurrentStorageUI().ifPresent(ui -> ui.putServerMetadata(key, metadata));
    }

    @RPCPacket("session.storageui.receive_metadata_map")
    public static void receiveMetadataMap(StorageMetadataMap map) {
        getCurrentStorageUI().ifPresent(ui -> ui.putAllServerMetadata(map.value()));
    }

    public static void createBoth() {
        RPCPacketDistributor.rpcToServer("session.storageui.create");
        ArosEngineClient.getEngine().storageManager().sessionManager().create(null);
    }

    public static void closeBoth() {
        RPCPacketDistributor.rpcToServer("session.storageui.close");
        ArosEngineClient.getEngine().storageManager().sessionManager().close(null);
    }

    private static Optional<StorageUI> getCurrentStorageUI() {
        if (!(Minecraft.getInstance().screen instanceof ModularUIScreen modularUIScreen)) return Optional.empty();
        if (!(modularUIScreen.modularUI.ui.rootElement instanceof StorageUI storageUI)) return Optional.empty();
        return Optional.of(storageUI);
    }
}
