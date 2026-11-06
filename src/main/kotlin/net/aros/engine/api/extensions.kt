package net.aros.engine.api

import net.aros.canon.core.Can
import net.aros.canon.core.state.StateKey
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.*

inline val String.id: ResourceLocation
    get() = ResourceLocation.parse(this)

inline val String.literal: Component
    get() = Component.literal(this)

inline val String.translation: Component
    get() = Component.translatable(this)

typealias Ticks = Int

inline val Int.ticks: Ticks
    get() = this

inline val Int.seconds: Ticks
    get() = ticks * 20

inline val Int.minutes: Ticks
    get() = seconds * 60

inline val Int.hours: Ticks
    get() = minutes * 60

infix fun Item.x(count: Int) = ItemStack(this, count)
infix fun Int.x(item: Item) = ItemStack(item, this)

fun <S, T> StateKey<S, T>.set(scope: S, value: T) = Can.set(this, scope, value)
fun <S, T> StateKey<S, T>.get(scope: S): T = Can.get(this, scope)

fun <T> StateKey<net.minecraft.util.Unit, T>.set(value: T) = Can.setGlobal(this, value)
fun <T> StateKey<net.minecraft.util.Unit, T>.get(): T = Can.getGlobal(this)

fun <T> Entity.set(stateKey: StateKey<UUID, T>, value: T) = Can.set(stateKey, uuid, value)
fun <T> Entity.get(stateKey: StateKey<UUID, T>): T = Can.get(stateKey, uuid)

fun <T> ServerLevel.set(stateKey: StateKey<ResourceKey<Level>, T>, value: T) = Can.set(stateKey, dimension(), value)
fun <T> ServerLevel.get(stateKey: StateKey<ResourceKey<Level>, T>): T = Can.get(stateKey, dimension())