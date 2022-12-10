package com.kneelawk.wiredredstone.compat.create

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.part.event.WRPartPreMoveEvent
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks

@Suppress("unused")
object CreateCompatImpl : CreateCompat {
    override fun init() {
        WRLog.log.info("Wired Redstone: enabling Create compatibility!")

        BlockMovementChecks.registerMovementNecessaryCheck { _, world, pos ->
            val event = WRPartPreMoveEvent()
            MultipartUtil.get(world, pos)?.fireEvent(event)

            if (event.movementNecessary) BlockMovementChecks.CheckResult.SUCCESS else BlockMovementChecks.CheckResult.PASS
        }
    }
}
