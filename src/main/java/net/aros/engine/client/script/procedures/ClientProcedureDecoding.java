package net.aros.engine.client.script.procedures;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import io.netty.buffer.Unpooled;
import net.aros.engine.common.script.procedure.ProcedureSerializationException;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
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

import static net.aros.engine.client.script.procedures.ProcedureParameterDeserializer.streamCodec;

public class ClientProcedureDecoding {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Class<?>, ProcedureParameterDeserializer<?>> PARAMETER_DESERIALIZERS = new HashMap<>();

    public static <T> void registerDeserializer(Class<T> type, ProcedureParameterDeserializer<T> deserializer) {
        PARAMETER_DESERIALIZERS.put(type, Objects.requireNonNull(deserializer));
    }

    public static Object @NotNull [] decodeProcedureArgs(
            ResourceLocation id, RegistryAccess access, byte[] bytes
    ) throws ProcedureSerializationException {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), access, ConnectionType.OTHER);

        try {
            return tryDecodeProcedureArgs(buffer, id);
        } finally {
            buffer.release();
        }
    }

    private static Object @NotNull [] tryDecodeProcedureArgs(RegistryFriendlyByteBuf buffer, ResourceLocation id)
            throws ProcedureSerializationException {
        int faults = 0;
        int len = buffer.readInt();
        Object[] args = new Object[len];
        for (int i = 0; i < len; i++) {
            var arg = tryDecodeArgument(id, buffer);
            if (arg == null)
                faults++;
            else
                args[i] = arg;
        }
        if (faults > 0) throw new ProcedureSerializationException();
        return args;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static Object tryDecodeArgument(ResourceLocation procId, RegistryFriendlyByteBuf buffer) {
        var classname = ByteBufCodecs.STRING_UTF8.decode(buffer);
        Class<?> type;
        try {
            type = Class.forName(classname);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Cannot deserialize procedure call for '{}': cannot load class {}", procId, classname);
            return null;
        }

        var deserializer = (ProcedureParameterDeserializer<Object>) PARAMETER_DESERIALIZERS.get(type);
        if (deserializer == null) {
            LOGGER.error("Cannot deserialize procedure call for '{}': no deserializer found for {}", procId, type.getName());
            return null;
        }
        var result = deserializer.deserialize(buffer);
        if (result.isError()) {
            LOGGER.warn("Cannot deserialize procesure call for '{}': {}", procId, result.error().orElseThrow().message());
            return null;
        }
        return result.getOrThrow();
    }

    static {
        registerDeserializer(Boolean.class, streamCodec(ByteBufCodecs.BOOL));
        registerDeserializer(Byte.class, streamCodec(ByteBufCodecs.BYTE));
        registerDeserializer(Short.class, streamCodec(ByteBufCodecs.SHORT));
        registerDeserializer(Integer.class, streamCodec(ByteBufCodecs.INT));
        registerDeserializer(Long.class, streamCodec(ByteBufCodecs.VAR_LONG));
        registerDeserializer(Float.class, streamCodec(ByteBufCodecs.FLOAT));
        registerDeserializer(Double.class, streamCodec(ByteBufCodecs.DOUBLE));
        registerDeserializer(byte[].class, streamCodec(ByteBufCodecs.BYTE_ARRAY));
        registerDeserializer(String.class, streamCodec(ByteBufCodecs.STRING_UTF8));
        registerDeserializer(ResourceLocation.class, streamCodec(ResourceLocation.STREAM_CODEC));
        registerDeserializer(UUID.class, streamCodec(UUIDUtil.STREAM_CODEC));
        registerDeserializer(Tag.class, streamCodec(ByteBufCodecs.TAG));
        registerDeserializer(CompoundTag.class, streamCodec(ByteBufCodecs.COMPOUND_TAG));
        registerDeserializer(PropertyMap.class, streamCodec(ByteBufCodecs.GAME_PROFILE_PROPERTIES));
        registerDeserializer(GameProfile.class, streamCodec(ByteBufCodecs.GAME_PROFILE));
        registerDeserializer(Component.class, streamCodec(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC));
        registerDeserializer(BlockPos.class, streamCodec(BlockPos.STREAM_CODEC));
        registerDeserializer(GlobalPos.class, streamCodec(GlobalPos.STREAM_CODEC));
        registerDeserializer(Rotations.class, streamCodec(Rotations.STREAM_CODEC));
        registerDeserializer(Vector3f.class, streamCodec(ByteBufCodecs.VECTOR3F));
        registerDeserializer(Quaternionf.class, streamCodec(ByteBufCodecs.QUATERNIONF));
        registerDeserializer(ChunkPos.class, streamCodec(NeoForgeStreamCodecs.CHUNK_POS));
        registerDeserializer(Item.class, streamCodec(ByteBufCodecs.registry(Registries.ITEM)));
        registerDeserializer(Block.class, streamCodec(ByteBufCodecs.registry(Registries.BLOCK)));
        //noinspection unchecked
        registerDeserializer((Class<EntityType<?>>) (Class<?>) EntityType.class, streamCodec(ByteBufCodecs.registry(Registries.ENTITY_TYPE)));
        registerDeserializer(MobEffect.class, streamCodec(ByteBufCodecs.registry(Registries.MOB_EFFECT)));
        registerDeserializer(SoundEvent.class, streamCodec(ByteBufCodecs.registry(Registries.SOUND_EVENT)));
        registerDeserializer(ItemStack.class, streamCodec(ItemStack.STREAM_CODEC));
        registerDeserializer(Level.class, (buffer) -> levelByDimension(ResourceKey.streamCodec(Registries.DIMENSION).decode(buffer)));
        registerDeserializer(Entity.class, (buffer)
                -> entityInDimension(ResourceKey.streamCodec(Registries.DIMENSION).decode(buffer), ByteBufCodecs.INT.decode(buffer),
                Entity.class));
        registerDeserializer(LivingEntity.class, (buffer)
                -> entityInDimension(ResourceKey.streamCodec(Registries.DIMENSION).decode(buffer), ByteBufCodecs.INT.decode(buffer),
                LivingEntity.class));
        registerDeserializer(Mob.class, (buffer)
                -> entityInDimension(ResourceKey.streamCodec(Registries.DIMENSION).decode(buffer), ByteBufCodecs.INT.decode(buffer),
                Mob.class));
        registerDeserializer(Player.class, (buffer)
                -> entityInDimension(ResourceKey.streamCodec(Registries.DIMENSION).decode(buffer), ByteBufCodecs.INT.decode(buffer),
                Player.class));
    }

    private static <E extends Entity> DataResult<E> entityInDimension(ResourceKey<Level> dimension, int id, Class<E> entityClass) {
        return levelByDimension(dimension).flatMap(level -> entityInLevel(level, id, entityClass));
    }

    private static <E extends Entity> DataResult<E> entityInLevel(Level level, int id, Class<E> entityClass) {
        Entity e = level.getEntity(id);
        if (e == null) return DataResult.error(() -> "Entity not in render distance");
        if (!entityClass.isAssignableFrom(e.getClass()))
            return DataResult.error(() -> "Entity is not instance of " + entityClass + ", got: " + e.getClass());
        return DataResult.success(entityClass.cast(e));
    }

    private static DataResult<Level> levelByDimension(ResourceKey<Level> dimension) {
        Level level = Minecraft.getInstance().level;
        if (level == null || !Objects.equals(level.dimension(), dimension))
            return DataResult.error(() -> "Cannot get not current level on client");
        return DataResult.success(level);
    }
}
