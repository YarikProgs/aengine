package net.aros.engine.common.storage;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ServerStorageUISession {
    @RPCPacket("session.storageui.create")
    public static void onCreate(@NotNull RPCSender sender) {
        if (sender.asPlayer() != null)
            StorageManager.sessionManager().create(sender.asPlayer());
    }

    @RPCPacket("session.storageui.close")
    public static void onClose(@NotNull RPCSender sender) {
        if (sender.asPlayer() != null)
            StorageManager.sessionManager().close(sender.asPlayer());
    }

    public static void sendInitialDataTo(ServerPlayer player, InitialData initialData) {
        RPCPacketDistributor.rpcToPlayer(player, "session.storageui.receive_initial_data", initialData);
    }

    public static void sendMetadataTo(ServerPlayer player, StorageKey<?> key, StorageMetadata metadata) {
        RPCPacketDistributor.rpcToPlayer(player, "session.storageui.receive_metadata", key, metadata);
    }

    public static <T> void sendContentUpdate(ServerPlayer player, ContentUpdate<T> update) {
        //noinspection RedundantCast,rawtypes
        RPCPacketDistributor.rpcToPlayer(player, "session.storageui.receive_content", (ContentUpdate) update);
    }
}
