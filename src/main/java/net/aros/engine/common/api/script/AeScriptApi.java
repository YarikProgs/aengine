package net.aros.engine.common.api.script;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.aros.canon.core.state.registration.EventSubscription;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.script.callables.CallContext;
import net.aros.engine.common.api.script.callables.EntityCall;
import net.aros.engine.common.api.script.callables.GlobalCall;
import net.aros.engine.common.api.script.callables.PlayerCall;
import net.aros.engine.common.api.script.callables.commandcodecs.CommandCodec;
import net.aros.engine.common.api.script.compilation.AeScript;
import net.aros.engine.common.api.storage.StorageEntry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class AeScriptApi {
    private static final Multimap<Class<?>, EventSubscription> EVENT_SUBSCRIPTIONS = ArrayListMultimap.create();
    private static final Map<Callable<?>, Consumer<? extends CallContext>> CALLABLES = new HashMap<>();
    private static final Map<Class<? extends CallContext>, CommandCodec<?>> CALL_CONTEXT_COMMAND_CODECS = new HashMap<>();

    public static void initialize() {
        registerCallContextCommandCodec(GlobalCall.class, GlobalCall.COMMAND_CODEC);
        registerCallContextCommandCodec(EntityCall.class, EntityCall.COMMAND_CODEC);
        registerCallContextCommandCodec(PlayerCall.class, PlayerCall.COMMAND_CODEC);
    }

    public static <T extends Event> void subscribeNeoForgeEvent(Class<T> type, Consumer<T> consumer) {
        NeoForge.EVENT_BUS.addListener(type, consumer);
        EVENT_SUBSCRIPTIONS.put(type, () -> NeoForge.EVENT_BUS.unregister(consumer));
    }

    public static <T extends CallContext> void registerCallContextCommandCodec(Class<T> type, CommandCodec<T> commandCodec) {
        CALL_CONTEXT_COMMAND_CODECS.put(type, commandCodec);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends CallContext> CommandCodec<T> getCallContextCommandCodec(Class<T> type) {
        return (CommandCodec<T>) CALL_CONTEXT_COMMAND_CODECS.get(type);
    }

    public static <T extends CallContext> void addCallable(Class<T> type, ResourceLocation id, Consumer<T> consumer) {
        CALLABLES.put(new Callable<>(type, id), consumer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CallContext> Consumer<T> getCallable(Class<T> contextType, ResourceLocation id) {
        return (Consumer<T>) CALLABLES.get(new Callable<>(contextType, id));
    }

    public static @NotNull Set<Callable<?>> getCallables() {
        return CALLABLES.keySet();
    }

    public static void clearRegistrations() {
        EVENT_SUBSCRIPTIONS.forEach((type, subscription) -> subscription.unsubscribe());
        EVENT_SUBSCRIPTIONS.clear();
        CALLABLES.clear();
    }

    public static void runScripts(Set<StorageEntry<AeScript>> scripts) {
        scripts.stream()
                .sorted(Comparator.comparing(StorageEntry::key))
                .forEach(entry -> runScript(entry.key(), entry.value().get()));
    }

    private static void runScript(ResourceLocation id, Runnable script) {
        try {
            script.run();
        } catch (RuntimeException e) {
            ArosEngine.LOGGER.error("Failed to execute AEngine script '{}' after storage reload", id, e);
        }
    }

    public record Callable<T extends CallContext>(Class<T> contextType, ResourceLocation identifier) {
    }
}
