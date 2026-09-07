package net.aros.engine.common.script.procedure;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static net.aros.engine.common.script.procedure.ProcedureParameterSerializer.streamCodec;

public class ProcedureEncoding {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Class<?>, ProcedureParameterSerializer<?>> PARAMETER_SERIALIZERS = new HashMap<>();

    public static <T> void registerSerializer(Class<T> type, ProcedureParameterSerializer<T> serializer) {
        PARAMETER_SERIALIZERS.put(type, Objects.requireNonNull(serializer));
    }

    public static byte[] encodeProcedureArgs(
            ResourceLocation id, RegistryAccess access, Object @NotNull ... args
    ) throws ProcedureSerializationException {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), access, ConnectionType.OTHER);

        try {
            tryEncodeProcedureArgs(buffer, id, args);
            return ByteBufUtil.getBytes(buffer);
        } finally {
            buffer.release();
        }
    }

    private static void tryEncodeProcedureArgs(@NotNull RegistryFriendlyByteBuf buffer, ResourceLocation id, Object @NotNull ... args)
            throws ProcedureSerializationException {
        int faults = 0;
        buffer.writeInt(args.length);
        for (Object arg : args) {
            if (!tryEncodeArgument(id, buffer, arg)) faults++;
        }
        if (faults > 0) throw new ProcedureSerializationException();
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean tryEncodeArgument(ResourceLocation procId, RegistryFriendlyByteBuf buffer, @NotNull T argument) {
        var serializer = (ProcedureParameterSerializer<T>) findSerializer(argument.getClass());
        if (serializer == null) {
            LOGGER.error("Cannot serialize procedure call for '{}': no serializer found for {}", procId, argument.getClass().getName());
            return false;
        }
        ByteBufCodecs.STRING_UTF8.encode(buffer, argument.getClass().getName());
        serializer.serialize(buffer, argument);
        return true;
    }

    private static @Nullable ProcedureParameterSerializer<?> findSerializer(Class<?> argumentType) {
        var serializer = PARAMETER_SERIALIZERS.get(argumentType);
        if (serializer != null) return serializer;

        for (var entry : PARAMETER_SERIALIZERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(argumentType)) {
                return entry.getValue();
            }
        }
        return null;
    }

    static {
        registerSerializer(Boolean.class, streamCodec(ByteBufCodecs.BOOL));
        registerSerializer(Byte.class, streamCodec(ByteBufCodecs.BYTE));
        registerSerializer(Short.class, streamCodec(ByteBufCodecs.SHORT));
        registerSerializer(Integer.class, streamCodec(ByteBufCodecs.INT));
        registerSerializer(Long.class, streamCodec(ByteBufCodecs.VAR_LONG));
        registerSerializer(Float.class, streamCodec(ByteBufCodecs.FLOAT));
        registerSerializer(Double.class, streamCodec(ByteBufCodecs.DOUBLE));
        registerSerializer(byte[].class, streamCodec(ByteBufCodecs.BYTE_ARRAY));
        registerSerializer(String.class, streamCodec(ByteBufCodecs.STRING_UTF8));
        registerSerializer(ResourceLocation.class, streamCodec(ResourceLocation.STREAM_CODEC));
        registerSerializer(UUID.class, streamCodec(UUIDUtil.STREAM_CODEC));
        registerSerializer(Tag.class, streamCodec(ByteBufCodecs.TAG));
        registerSerializer(CompoundTag.class, streamCodec(ByteBufCodecs.COMPOUND_TAG));
        registerSerializer(PropertyMap.class, streamCodec(ByteBufCodecs.GAME_PROFILE_PROPERTIES));
        registerSerializer(GameProfile.class, streamCodec(ByteBufCodecs.GAME_PROFILE));
        registerSerializer(Component.class, streamCodec(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC));
        registerSerializer(BlockPos.class, streamCodec(BlockPos.STREAM_CODEC));
        registerSerializer(GlobalPos.class, streamCodec(GlobalPos.STREAM_CODEC));
        registerSerializer(Rotations.class, streamCodec(Rotations.STREAM_CODEC));
        registerSerializer(Vector3f.class, streamCodec(ByteBufCodecs.VECTOR3F));
        registerSerializer(Quaternionf.class, streamCodec(ByteBufCodecs.QUATERNIONF));
        registerSerializer(ChunkPos.class, streamCodec(NeoForgeStreamCodecs.CHUNK_POS));
        registerSerializer(Item.class, streamCodec(ByteBufCodecs.registry(Registries.ITEM)));
        registerSerializer(Block.class, streamCodec(ByteBufCodecs.registry(Registries.BLOCK)));
        //noinspection unchecked
        registerSerializer((Class<EntityType<?>>) (Class<?>) EntityType.class, streamCodec(ByteBufCodecs.registry(Registries.ENTITY_TYPE)));
        registerSerializer(MobEffect.class, streamCodec(ByteBufCodecs.registry(Registries.MOB_EFFECT)));
        registerSerializer(SoundEvent.class, streamCodec(ByteBufCodecs.registry(Registries.SOUND_EVENT)));
        registerSerializer(ItemStack.class, streamCodec(ItemStack.STREAM_CODEC));
        registerSerializer(Level.class, (buffer, parameter) -> {
            ResourceKey.streamCodec(Registries.DIMENSION).encode(buffer, parameter.dimension());
        });
        registerSerializer(Entity.class, (buffer, parameter) -> {
            ResourceKey.streamCodec(Registries.DIMENSION).encode(buffer, parameter.level().dimension());
            ByteBufCodecs.INT.encode(buffer, parameter.getId());
        });
    }
}
