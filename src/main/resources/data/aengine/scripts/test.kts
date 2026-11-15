package data.aengine.scripts

import net.aros.canon.core.state.StateKey
import net.aros.canon.core.state.scope.BuiltinScopeTypes
import net.aros.canon.core.state.type.BuiltinStateTypes
import net.aros.engine.api.*
import net.aros.engine.api.script.callClient
import net.aros.engine.api.script.on
import net.aros.engine.api.script.onStateRegistration
import net.aros.engine.api.script.proc
import net.aros.engine.common.ArosEngine
import net.aros.engine.common.storage.event.StorageReloadEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import net.neoforged.neoforge.event.tick.ServerTickEvent

val diamondsAllowed = StateKey(
    BuiltinScopeTypes.ENTITY,
    "example:diamonds_allowed".id,
    BuiltinStateTypes.BOOL
)

onStateRegistration {
    state(diamondsAllowed) {
        listen { uuid, _, current ->
            server.playerList.getPlayer(uuid)?.sendSystemMessage(
                if (current)
                    "Ты испытал боль, и теперь можешь получать алмазы".literal
                else
                    "Отныне доступ к алмазом тебе ограничен".literal
            )
        }
    }
}

proc<ServerPlayer, Int, Int, Unit>("mymod:give_money_and_other".id) { player, money, other ->

}

proc<ServerPlayer, Boolean, Unit>("mymod:diamonds/set_allowed".id) { player, allowed ->
    player.set(diamondsAllowed, allowed)
    if (allowed) {
        player.addItem(Items.DIAMOND x 1)
    } else {
        player.hurt(player.damageSources().genericKill(), 1.0F)

        callClient { player(player, "mymod:test.client".id) }
    }
}

on<ServerTickEvent.Post> {
    server.playerList.players.forEach { player ->
        if (player.get(diamondsAllowed)) return@forEach
        player.inventory.items.forEach { stack ->
            if (stack.`is`(Items.DIAMOND))
                stack.shrink(64)
        }
    }
}

on<StorageReloadEvent.After> {
    ArosEngine.LOGGER.info("[test.kts] Hello!")
}
