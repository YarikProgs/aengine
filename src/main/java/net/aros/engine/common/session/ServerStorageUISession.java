package net.aros.engine.common.session;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadataMap;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ServerStorageUISession {
    @RPCPacket("session.storageui.create")
    public static void onCreate(@NotNull RPCSender sender) {
        if (sender.asPlayer() != null)
            ArosEngine.getEngine().storageManager().sessionManager().create(sender.asPlayer());
    }

    @RPCPacket("session.storageui.close")
    public static void onClose(@NotNull RPCSender sender) {
        if (sender.asPlayer() != null)
            ArosEngine.getEngine().storageManager().sessionManager().close(sender.asPlayer());
    }

    public static void sendMetadataTo(ServerPlayer player, StorageKey<?> key, StorageMetadata metadata) {
        RPCPacketDistributor.rpcToPlayer(player, "session.storageui.receive_metadata", key, metadata);
    }

    public static void sendMetadataMapTo(ServerPlayer player, Map<StorageKey<?>, StorageMetadata> metadataMap) {
        RPCPacketDistributor.rpcToPlayer(player, "session.storageui.receive_metadata_map", new StorageMetadataMap(metadataMap));
    }
}
