package com.kneelawk.wiredredstone.compat.wthit.impl

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartUtil
import alexiil.mc.lib.multipart.impl.MultipartBlock
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.part.AbstractRedstoneWirePart
import com.kneelawk.wiredredstone.part.GateRepeaterPart
import com.kneelawk.wiredredstone.part.WRPart
import mcp.mobius.waila.api.*
import net.minecraft.util.math.Vec3d

@Suppress("unused")
class PartPlugin : IWailaPlugin, IBlockComponentProvider {
    override fun register(registrar: IRegistrar) {
        // referencing an impl class directly isn't great, but it's the only way we can select multipart blocks
        registrar.addComponent(this, TooltipPosition.BODY, MultipartBlock::class.java)
    }

    override fun appendBody(tooltip: ITooltip, accessor: IBlockAccessor, config: IPluginConfig) {
        getSelectedPart(accessor)?.let { part ->
            if (part is AbstractRedstoneWirePart) {
                tooltip.addLine(WRConstants.tooltip("redstone_wire.power", part.power))
            }
            if (part is GateRepeaterPart) {
                tooltip.addLine(WRConstants.tooltip("gate_repeater.delay", (part.delay + 1).toFloat() / 2f))
            }
        }
    }

    private fun getSelectedPart(accessor: IBlockAccessor): WRPart? {
        return MultipartUtil.get(accessor.world, accessor.position)?.let { container ->
            val vec = accessor.hitResult.pos.subtract(Vec3d.of(accessor.position))
            container.getFirstPart { doesContain(it, vec) }
        } as? WRPart
    }

    private fun doesContain(part: AbstractPart, vec: Vec3d): Boolean {
        val shape = part.outlineShape
        for (box in shape.boundingBoxes) {
            if (box.expand(0.01).contains(vec)) {
                return true
            }
        }
        return false
    }
}
