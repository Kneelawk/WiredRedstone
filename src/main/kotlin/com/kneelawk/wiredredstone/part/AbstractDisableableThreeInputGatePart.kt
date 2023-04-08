package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.node.WRBlockNodes
import com.kneelawk.wiredredstone.util.BoundingBoxMap
import com.kneelawk.wiredredstone.util.getBoolean
import com.kneelawk.wiredredstone.util.getWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import java.util.*

abstract class AbstractDisableableThreeInputGatePart : AbstractThreeInputGatePart {
    val enabledInputs = EnumSet.noneOf(InputType::class.java)

    var inputRightEnabled: Boolean
        get() = enabledInputs.contains(InputType.RIGHT)
        private set(value) {
            if (value) enabledInputs.add(InputType.RIGHT) else enabledInputs.remove(InputType.RIGHT)
        }
    var inputBackEnabled: Boolean
        get() = enabledInputs.contains(InputType.BACK)
        private set(value) {
            if (value) enabledInputs.add(InputType.BACK) else enabledInputs.remove(InputType.BACK)
        }
    var inputLeftEnabled: Boolean
        get() = enabledInputs.contains(InputType.LEFT)
        private set(value) {
            if (value) enabledInputs.add(InputType.LEFT) else enabledInputs.remove(InputType.LEFT)
        }

    abstract val inputShapes: BoundingBoxMap<InputType>

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputRightPower: Int, inputBackPower: Int, inputLeftPower: Int, outputPower: Int, outputReversePower: Int,
        inputRightEnabled: Boolean, inputBackEnabled: Boolean, inputLeftEnabled: Boolean
    ) : super(
        definition, holder, side, connections, direction, inputRightPower, inputBackPower, inputLeftPower, outputPower,
        outputReversePower
    ) {
        if (inputRightEnabled) enabledInputs.add(InputType.RIGHT)
        if (inputBackEnabled) enabledInputs.add(InputType.BACK)
        if (inputLeftEnabled) enabledInputs.add(InputType.LEFT)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        if (tag.getBoolean("inputRightEnabled", true)) enabledInputs.add(InputType.RIGHT)
        if (tag.getBoolean("inputBackEnabled", true)) enabledInputs.add(InputType.BACK)
        if (tag.getBoolean("inputLeftEnabled", true)) enabledInputs.add(InputType.LEFT)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        if (buffer.readBoolean()) enabledInputs.add(InputType.RIGHT)
        if (buffer.readBoolean()) enabledInputs.add(InputType.BACK)
        if (buffer.readBoolean()) enabledInputs.add(InputType.LEFT)
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()

        tag.putBoolean("inputRightEnabled", inputRightEnabled)
        tag.putBoolean("inputBackEnabled", inputBackEnabled)
        tag.putBoolean("inputLeftEnabled", inputLeftEnabled)

        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeBoolean(inputRightEnabled)
        buffer.writeBoolean(inputBackEnabled)
        buffer.writeBoolean(inputLeftEnabled)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeBoolean(inputRightEnabled)
        buffer.writeBoolean(inputBackEnabled)
        buffer.writeBoolean(inputLeftEnabled)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        inputRightEnabled = buffer.readBoolean()
        inputBackEnabled = buffer.readBoolean()
        inputLeftEnabled = buffer.readBoolean()
    }

    override fun getDynamicShape(partialTicks: Float, hitVec: Vec3d): VoxelShape {
        val hit = hitVec.subtract(Vec3d.of(getPos()))

        inputShapes.getTouching(hit, orientation)?.let { touching ->
            return touching.shape
        }

        return super.getDynamicShape(partialTicks, hitVec)
    }

    override fun onUse(player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val pos = getPos()
        val hitVec = hit.pos.subtract(Vec3d.of(pos))

        inputShapes.getTouching(hitVec, orientation)?.let { touching ->
            val world = getWorld()
            if (world !is ServerWorld) {
                return ActionResult.SUCCESS
            }

            val input = touching.key
            if (enabledInputs.contains(input)) {
                enabledInputs.remove(input)
                updateInputPower(0, input)
            } else {
                enabledInputs.add(input)
                updateInputPower(calculateInputPower(input), input)
            }

            WRBlockNodes.WIRE_NET.getGraphWorld(world).updateNodes(pos)

            return ActionResult.CONSUME
        }

        return super.onUse(player, hand, hit)
    }
}
