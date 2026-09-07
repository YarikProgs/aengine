@file:Suppress("NOTHING_TO_INLINE")

package net.aros.engine.client.api.script

import net.aros.engine.client.script.procedures.ClientProcedures
import net.aros.engine.client.script.events.ClientEventSubscriptions
import net.aros.engine.common.script.procedure.Procedure
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.Event
import org.jetbrains.annotations.ApiStatus
import kotlin.jvm.javaObjectType
import kotlin.reflect.KClass

inline fun <reified T : Event> onClient(noinline body: T.() -> Unit) {
    ClientEventSubscriptions.subscribe(T::class.java, body)
}

inline fun <reified R> call(id: ResourceLocation, vararg args: Any): R? {
    return ClientProcedures.call(id, args) as? R
}

@ApiStatus.Internal
fun addProc(id: ResourceLocation, vararg parameterTypes: KClass<out Any>, body: (args: Array<Any>) -> Any) =
    ClientProcedures.add(Procedure(id, parameterTypes.map { it.javaObjectType }.toTypedArray(), body))

inline fun <R : Any> proc(id: ResourceLocation, noinline body: () -> R) =
    addProc(id) { return@addProc body() }

inline fun <reified P1 : Any, R : Any> proc(id: ResourceLocation, noinline body: (P1) -> R) =
    addProc(id, P1::class) { arguments ->
        return@addProc body(arguments[0] as P1)
    }

inline fun <reified P1 : Any, reified P2 : Any, R : Any> proc(id: ResourceLocation, noinline body: (P1, P2) -> R) =
    addProc(id, P1::class, P2::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2)
    }

inline fun <reified P1 : Any, reified P2 : Any, reified P3 : Any, R : Any> proc(id: ResourceLocation, noinline body: (P1, P2, P3) -> R) =
    addProc(id, P1::class, P2::class, P3::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2, arguments[2] as P3)
    }

inline fun <reified P1 : Any, reified P2 : Any, reified P3 : Any, reified P4 : Any, R : Any> proc(id: ResourceLocation, noinline body: (P1, P2, P3, P4) -> R) =
    addProc(id, P1::class, P2::class, P3::class, P4::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2, arguments[2] as P3, arguments[3] as P4)
    }

inline fun <reified P1 : Any, reified P2 : Any, reified P3 : Any, reified P4 : Any, reified P5 : Any, R : Any> proc(
    id: ResourceLocation,
    noinline body: (P1, P2, P3, P4, P5) -> R
) =
    addProc(id, P1::class, P2::class, P3::class, P4::class, P5::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2, arguments[2] as P3, arguments[3] as P4, arguments[4] as P5)
    }

inline fun <reified P1 : Any, reified P2 : Any, reified P3 : Any, reified P4 : Any, reified P5 : Any, reified P6 : Any, R : Any> proc(
    id: ResourceLocation,
    noinline body: (P1, P2, P3, P4, P5, P6) -> R
) =
    addProc(id, P1::class, P2::class, P3::class, P4::class, P5::class, P6::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2, arguments[2] as P3, arguments[3] as P4, arguments[4] as P5, arguments[5] as P6)
    }

inline fun <reified P1 : Any, reified P2 : Any, reified P3 : Any, reified P4 : Any, reified P5 : Any, reified P6 : Any, reified P7 : Any, R : Any> proc(
    id: ResourceLocation,
    noinline body: (P1, P2, P3, P4, P5, P6, P7) -> R
) =
    addProc(id, P1::class, P2::class, P3::class, P4::class, P5::class, P6::class, P7::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2, arguments[2] as P3, arguments[3] as P4, arguments[4] as P5, arguments[5] as P6, arguments[6] as P7)
    }

inline fun <reified P1 : Any, reified P2 : Any, reified P3 : Any, reified P4 : Any, reified P5 : Any, reified P6 : Any, reified P7 : Any, reified P8 : Any, R : Any> proc(
    id: ResourceLocation,
    noinline body: (P1, P2, P3, P4, P5, P6, P7, P8) -> R
) =
    addProc(id, P1::class, P2::class, P3::class, P4::class, P5::class, P6::class, P7::class, P8::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2, arguments[2] as P3, arguments[3] as P4, arguments[4] as P5, arguments[5] as P6, arguments[6] as P7, arguments[7] as P8)
    }

inline fun <reified P1 : Any, reified P2 : Any, reified P3 : Any, reified P4 : Any, reified P5 : Any, reified P6 : Any, reified P7 : Any, reified P8 : Any, reified P9 : Any, R : Any> proc(
    id: ResourceLocation,
    noinline body: (P1, P2, P3, P4, P5, P6, P7, P8, P9) -> R
) =
    addProc(id, P1::class, P2::class, P3::class, P4::class, P5::class, P6::class, P7::class, P8::class, P9::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2, arguments[2] as P3, arguments[3] as P4, arguments[4] as P5, arguments[5] as P6, arguments[6] as P7, arguments[7] as P8, arguments[8] as P9)
    }

inline fun <reified P1 : Any, reified P2 : Any, reified P3 : Any, reified P4 : Any, reified P5 : Any, reified P6 : Any, reified P7 : Any, reified P8 : Any, reified P9 : Any, reified P10 : Any, R : Any> proc(
    id: ResourceLocation,
    noinline body: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10) -> R
) =
    addProc(id, P1::class, P2::class, P3::class, P4::class, P5::class, P6::class, P7::class, P8::class, P9::class, P10::class) { arguments ->
        return@addProc body(arguments[0] as P1, arguments[1] as P2, arguments[2] as P3, arguments[3] as P4, arguments[4] as P5, arguments[5] as P6, arguments[6] as P7, arguments[7] as P8, arguments[8] as P9, arguments[9] as P10)
    }
val minecraft: Minecraft
    get() = Minecraft.getInstance()