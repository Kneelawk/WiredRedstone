package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.api.util.LinkPos
import com.kneelawk.graphlib.api.util.NodePos
import com.kneelawk.wiredredstone.WRConstants.str
import com.kneelawk.wiredredstone.node.PowerlineLinkKey
import com.kneelawk.wiredredstone.node.WRBlockNodes
import com.kneelawk.wiredredstone.part.PowerlineConnectorPart
import com.kneelawk.wiredredstone.util.getSelectedPart
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Vec3d

class PowerlineWireItem(settings: Settings) : Item(settings) {
    companion object {
        private const val MAX_DISTANCE = 64.0
        private const val MAX_DISTANCE_SQR = MAX_DISTANCE * MAX_DISTANCE
        private val KEY = str("powerline_connector_pos")

        fun hasPosition(stack: ItemStack): Boolean {
            return stack.getSubNbt(KEY) != null
        }

        fun setPosition(stack: ItemStack, pos: NodePos) {
            stack.setSubNbt(KEY, pos.toNbt())
        }

        fun getPosition(stack: ItemStack): NodePos? {
            return stack.getSubNbt(KEY)?.let { NodePos.fromNbt(it, WRBlockNodes.WIRE_NET) }
        }

        fun removePosition(stack: ItemStack) {
            stack.removeSubNbt(KEY)
        }
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return hasPosition(stack)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val pos = context.blockPos
        val hitPos = context.hitPos.subtract(Vec3d.of(pos))
        val stack = context.stack

        val part = MultipartUtil.get(world, pos)?.getSelectedPart(hitPos) as? PowerlineConnectorPart
            ?: return ActionResult.FAIL

        if (world.isClient || world !is ServerWorld) return ActionResult.CONSUME

        val nodePos = NodePos(pos, part.getBlockNode())
        val existingPos = getPosition(stack)

        if (existingPos != null) {
            // connect the two nodes
            if (existingPos.pos.getSquaredDistance(pos) <= MAX_DISTANCE_SQR) {
                val graphWorld = WRBlockNodes.WIRE_NET.getServerGraphWorld(world)

                if (graphWorld.linkExistsAt(LinkPos(existingPos, nodePos, PowerlineLinkKey))) {
                    graphWorld.disconnectNodes(existingPos, nodePos, PowerlineLinkKey)
                } else {
                    graphWorld.connectNodes(existingPos, nodePos, PowerlineLinkKey)
                }

                removePosition(stack)

                return ActionResult.SUCCESS
            }
        } else {
            setPosition(stack, nodePos)
        }

        return ActionResult.FAIL
    }
}
