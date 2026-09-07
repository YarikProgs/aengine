package net.aros.engine.common.script.events;

import net.aros.canon.core.state.registration.EventSubscription;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventSubscriptions {
    private static final List<EventSubscription> EVENT_SUBSCRIPTIONS = new ArrayList<>();

    public static <T extends Event> void subscribe(Class<T> type, Consumer<T> consumer) {
        IEventBus bus = IModBusEvent.class.isAssignableFrom(type)
                ? ModLoadingContext.get().getActiveContainer().getEventBus()
                : NeoForge.EVENT_BUS;
        if (bus == null) return;

        bus.addListener(type, consumer);
        EVENT_SUBSCRIPTIONS.add(() -> bus.unregister(consumer));
    }

    public static void clear() {
        EVENT_SUBSCRIPTIONS.forEach(EventSubscription::unsubscribe);
        EVENT_SUBSCRIPTIONS.clear();
    }
}
