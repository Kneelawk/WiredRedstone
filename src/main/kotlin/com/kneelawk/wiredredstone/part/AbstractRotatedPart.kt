package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartPreTransformEvent
import alexiil.mc.lib.multipart.api.event.PartTransformEvent
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.node.WRBlockNodes
import com.kneelawk.wiredredstone.tag.WRItemTags
import com.kneelawk.wiredredstone.util.*
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

abstract class AbstractRotatedPart : AbstractConnectablePart, RotatedPart {
    var direction: Direction
        private set

    val orientation: SidedOrientation
        get() = SidedOrientation(side, direction)

    private var oldSide = side

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction
    ) : super(definition, holder, side, connections) {
        DirectionUtils.assertHorizontal(direction)
        this.direction = direction
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        direction = DirectionUtils.HORIZONTALS[tag.getByte("direction").toInt().coerceIn(0..3)]
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        direction = DirectionUtils.HORIZONTALS[buffer.readFixedBits(2).coerceIn(0..3)]
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("direction", direction.horizontal.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(direction.horizontal, 2)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(direction.horizontal, 2)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        direction = DirectionUtils.HORIZONTALS[buffer.readFixedBits(2).coerceIn(0..3)]
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        bus.addContextlessListener(this, PartPreTransformEvent::class.java) {
            oldSide = side
        }

        bus.addListener(this, PartTransformEvent::class.java) { e ->
            val oldDirection = direction
            val old = RotationUtils.rotatedDirection(oldSide, oldDirection)
            val newSide = e.transformation.map(oldSide)
            val new = e.transformation.map(old)
            direction = RotationUtils.unrotatedDirection(newSide, new)

            assert(DirectionUtils.isHorizontal(direction)) {
                "Ended up with non-planar direction after rotation!\n" +
                        "Transformation: ${e.transformation}\n" +
                        "Old Direction : $oldDirection\n" +
                        "Old Un-Rotated: $old\n" +
                        "Old Side      : $oldSide\n" +
                        "New Side      : $newSide\n" +
                        "New Un-Rotated: $new\n" +
                        "New Direction : $direction"
            }
        }
    }

    override fun getDynamicShape(partialTicks: Float, hitVec: Vec3d): VoxelShape {
        if (isClientSide()) {
            val player = MinecraftClient.getInstance().player
            if (player != null) {
                for (item in player.itemsHand) {
                    if (item.isIn(WRItemTags.SCREW_DRIVERS)) return VoxelShapes.empty()
                }
            }
        }

        return super.getDynamicShape(partialTicks, hitVec)
    }

    override fun onUse(player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val superRes = super.onUse(player, hand, hit)
        if (superRes != ActionResult.PASS) return superRes

        if (!player.canModifyBlocks()) return ActionResult.PASS

        for (item in player.itemsHand) {
            if (item.isIn(WRItemTags.SCREW_DRIVERS)) {
                if (hit.side != side.opposite) return ActionResult.FAIL

                if (isClientSide()) return ActionResult.SUCCESS

                val hitPosOffset = hit.pos.subtract(Vec3d.ofCenter(hit.blockPos))
                val zeroed = hitPosOffset.withAxis(side.axis, 0.0)
                val sideSide = Direction.getFacing(zeroed.x, zeroed.y, zeroed.z)
                val hitDir = DirectionUtils.makeHorizontal(RotationUtils.unrotatedDirection(side, sideSide))

                if (player.isSneaking) {
                    onMirror(hitDir.axis)
                } else {
                    onRotate(hitDir)
                }

                onPostScrewDriver()

                return ActionResult.CONSUME
            }
        }

        return ActionResult.PASS
    }

    open fun onRotate(to: Direction) {
        direction = to
    }

    open fun onMirror(axis: Axis) {
        if (direction.axis == axis) {
            direction = direction.opposite
        }
    }

    open fun onPostScrewDriver() {
        val world = getWorld()
        if (world is ServerWorld) {
            redraw()
            WRBlockNodes.WIRE_NET.getServerGraphWorld(world).updateConnections(getSidedPos())
        }
    }
}
