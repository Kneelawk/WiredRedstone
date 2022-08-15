package com.kneelawk.wiredredstone.compat.create

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.part.AbstractSidedPart
import com.kneelawk.wiredredstone.part.SidedPart
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks.CheckResult

@Suppress("unused")
object CreateCompatImpl : CreateCompat {
    override fun init() {
        WRLog.log.info("Wired Redstone: enabling Create compatibility!")

        BlockMovementChecks.registerMovementNecessaryCheck { _, world, pos ->
            var sidedPartPresent = false

            MultipartUtil.get(world, pos)?.getParts(SidedPart::class.java)?.forEach {
                // TODO: there should probably be an interface specifically for this
                (it as? AbstractSidedPart)?.setNoBreak()
                sidedPartPresent = true
            }

            if (sidedPartPresent) CheckResult.SUCCESS else CheckResult.PASS
        }
    }
}
