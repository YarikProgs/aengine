package assets.aengine.scripts

import net.aros.engine.api.id
import net.aros.engine.client.api.script.minecraft
import net.aros.engine.client.api.script.proc
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

proc<Unit>("mymod:test.client".id) {
    minecraft.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.ALLAY_DEATH, 1F))
}