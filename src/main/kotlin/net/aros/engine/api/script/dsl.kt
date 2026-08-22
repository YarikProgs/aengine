@file:Suppress("NOTHING_TO_INLINE")

package net.aros.engine.api.script

import net.aros.canon.event.custom.StateRegistrationEvent
import net.aros.engine.common.api.script.AeScriptApi
import net.aros.engine.common.api.script.callables.CallContext
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.Event

inline fun <reified T : Event> on(noinline body: T.() -> Unit) {
    AeScriptApi.subscribeNeoForgeEvent(T::class.java, body)
}

inline fun onStateRegistration(noinline body: StatesRegistrationDsl.() -> Unit) {
    on<StateRegistrationEvent> {
        body(StatesRegistrationDsl(this))
    }
}

inline fun <reified T : CallContext> callable(id: ResourceLocation, noinline body: T.() -> Unit) {
    AeScriptApi.addCallable(T::class.java, id, body)
}