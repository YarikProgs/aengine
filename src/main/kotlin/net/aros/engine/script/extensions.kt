package net.aros.engine.script

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

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