package com.kneelawk.wiredredstone.compat.create

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.part.NoBreakPart
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks.CheckResult

@Suppress("unused")
object CreateCompatImpl : CreateCompat {
    override fun init() {
        WRLog.log.info("[Wired Redstone] Enabling Create compatibility!")

        BlockMovementChecks.registerMovementNecessaryCheck { _, world, pos ->
            var sidedPartPresent = false

            MultipartUtil.get(world, pos)?.getParts(NoBreakPart::class.java)?.forEach {
                it.setNoBreak()
                sidedPartPresent = true
            }

            if (sidedPartPresent) CheckResult.SUCCESS else CheckResult.PASS
        }
    }
}
