package net.aros.engine.api.script

import net.aros.canon.core.Can
import net.aros.canon.core.state.StateKey
import net.aros.canon.core.state.registration.StateRegistration
import net.aros.canon.event.ChangeEvent
import net.aros.canon.event.custom.StateRegistrationEvent
import net.neoforged.bus.api.Event

class StatesRegistrationDsl(
    private val event: StateRegistrationEvent
) {
    fun <S, T> state(key: StateKey<S, T>) {
        state(key) {}
    }

    fun <S, T> state(key: StateKey<S, T>, body: StateRegistrationDsl<S, T>.() -> Unit) {
        body(StateRegistrationDsl(event.registerStateKey(key)))
    }
}

class StateRegistrationDsl<S, T>(
    private val registration: StateRegistration<S, T>
) {
    inline val server
        get() = Can.server()

    fun listen(listener: ChangeEvent<S, T>) {
        registration.addChangeListener(listener)
    }

    inline fun <reified E : Event> subscribe(handler: StateRegistration.StateEventHandler<S, T, E>) {
        subscribe(E::class.java, handler)
    }

    fun <E : Event> subscribe(eventType: Class<E>, handler: StateRegistration.StateEventHandler<S, T, E>) {
        registration.addEventListener(eventType, handler)
    }
}