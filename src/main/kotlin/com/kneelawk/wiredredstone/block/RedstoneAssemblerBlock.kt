package com.kneelawk.wiredredstone.block

import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity
import com.kneelawk.wiredredstone.blockentity.WRBlockEntities
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class RedstoneAssemblerBlock(settings: Settings) : BlockWithEntity(settings) {
    companion object {
        val FACING = HorizontalFacingBlock.FACING
        val LIT = Properties.LIT
    }

    init {
        defaultState = defaultState.with(FACING, Direction.NORTH).with(LIT, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING, LIT)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        RedstoneAssemblerBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World, state: BlockState, type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? = checkType(
        type, WRBlockEntities.REDSTONE_ASSEMBLER, if (!world.isClient) RedstoneAssemblerBlockEntity::tick else null
    )

    override fun getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

    override fun onUse(
        state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult
    ): ActionResult {
        return if (world.isClient) {
            ActionResult.SUCCESS
        } else {
            val be = world.getBlockEntity(pos) as? RedstoneAssemblerBlockEntity ?: return ActionResult.FAIL
            player.openHandledScreen(be)
            ActionResult.CONSUME
        }
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState =
        defaultState.with(AbstractFurnaceBlock.FACING, ctx.playerFacing.opposite)

    override fun onPlaced(
        world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack
    ) {
        if (itemStack.hasCustomName()) {
            val blockEntity = world.getBlockEntity(pos) as? RedstoneAssemblerBlockEntity ?: return
            blockEntity.customName = itemStack.name
        }
    }

    override fun onStateReplaced(
        state: BlockState, world: World, pos: BlockPos?, newState: BlockState, moved: Boolean
    ) {
        if (!state.isOf(newState.block)) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is RedstoneAssemblerBlockEntity) {
                if (world is ServerWorld) {
                    ItemScatterer.spawn(world, pos, blockEntity)
                }
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean = true

    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState? {
        return state.with(AbstractFurnaceBlock.FACING, rotation.rotate(state.get(AbstractFurnaceBlock.FACING)))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState? {
        return state.rotate(mirror.getRotation(state.get(AbstractFurnaceBlock.FACING)))
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if (state.get(AbstractFurnaceBlock.LIT)) {
            val x = pos.x.toDouble() + 0.5
            val y = pos.y.toDouble()
            val z = pos.z.toDouble() + 0.5
            if (random.nextDouble() < 0.1) {
                world.playSound(
                    x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f, false
                )
            }

            val direction = state.get(AbstractFurnaceBlock.FACING)
            val axis = direction.axis
            val faceX = random.nextDouble() * 0.6 - 0.3
            val particleX = if (axis === Direction.Axis.X) direction.offsetX.toDouble() * 0.52 else faceX
            val particleY = random.nextDouble() * 6.0 / 16.0 + (1.0 / 16.0)
            val particleZ = if (axis === Direction.Axis.Z) direction.offsetZ.toDouble() * 0.52 else faceX
            world.addParticle(ParticleTypes.SMOKE, x + particleX, y + particleY, z + particleZ, 0.0, 0.0, 0.0)
            world.addParticle(ParticleTypes.FLAME, x + particleX, y + particleY, z + particleZ, 0.0, 0.0, 0.0)
        }
    }
}
