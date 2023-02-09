package com.kneelawk.wiredredstone.client.screen

import com.kneelawk.wiredredstone.WRConstants.gui
import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.tooltip
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity
import com.kneelawk.wiredredstone.config.AssemblerConfig
import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class RedstoneAssemblerScreen(handler: RedstoneAssemblerScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<RedstoneAssemblerScreenHandler>(handler, playerInventory, title) {
    companion object {
        const val BACKGROUND_WIDTH = 176
        const val BACKGROUND_HEIGHT = 224

        private val TEXTURE = id("textures/gui/container/redstone_assembler.png")
    }

    init {
        backgroundWidth = BACKGROUND_WIDTH
        backgroundHeight = BACKGROUND_HEIGHT
        playerInventoryTitleY = backgroundHeight - 94
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionShader)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShaderTexture(0, TEXTURE)

        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2

        matrices.push()
        matrices.translate(x.toDouble(), y.toDouble(), 0.0)

        // draw background
        drawTexture(matrices, 0, 0, 0, 0, backgroundWidth, backgroundHeight)

        // draw energy bar
        val energyBar = handler.energyBar
        drawTexture(matrices, 11, 50 - energyBar, 230, 32 - energyBar, 10, energyBar)

        // draw regular crafting grid and crafting table switch
        if (handler.mode == RedstoneAssemblerBlockEntity.Mode.CRAFTING_TABLE) {
            drawTexture(matrices, 34, 16, 176, 0, 54, 54)
            drawTexture(matrices, 133, 59, 176, 85, 18, 10)
        }

        // draw use crafting items switch
        if (!handler.useCraftingItems) {
            drawTexture(matrices, 133, 75, 176, 95, 18, 10)
        }

        // draw cooking flames
        if (handler.isBurning) {
            val fuelProgress = handler.getFuelProgress()
            drawTexture(matrices, 8, 54 + 12 - fuelProgress, 176, 54 + 12 - fuelProgress, 14, fuelProgress + 1)
        }

        // draw completion arrow
        val cookProgress = handler.getCookProgress()
        drawTexture(matrices, 94, 34, 176, 68, cookProgress + 1, 16)

        matrices.pop()
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, titleX.toFloat(), titleY.toFloat(), 0xEEEEEE)
        textRenderer.draw(matrices, gui("redstone_assembler.input"), 29f, 82f, 0xEEEEEE)
        textRenderer.draw(
            matrices, playerInventoryTitle, playerInventoryTitleX.toFloat(), playerInventoryTitleY.toFloat(), 0xEEEEEE
        )
    }

    override fun drawMouseoverTooltip(matrices: MatrixStack, x: Int, y: Int) {
        super.drawMouseoverTooltip(matrices, x, y)

        val mx = x - this.x
        val my = y - this.y

        // energy tooltip
        if (mx in 10..21 && my in 17..50) {
            renderTooltip(
                matrices,
                tooltip("redstone_assembler.energy", handler.energyValue, AssemblerConfig.instance.energyCapacity),
                x, y
            )
        }

        // mode tooltip
        if (mx in 130..153 && my in 56..71) {
            val modeText = when (handler.mode) {
                RedstoneAssemblerBlockEntity.Mode.ASSEMBLER -> tooltip(
                    "redstone_assembler.mode.assembler"
                ).styled { it.withColor(Formatting.RED).withBold(true) }
                RedstoneAssemblerBlockEntity.Mode.CRAFTING_TABLE -> tooltip(
                    "redstone_assembler.mode.crafting_table"
                ).styled { it.withColor(Formatting.GOLD).withBold(true) }
            }

            renderTooltip(matrices, tooltip("redstone_assembler.mode", modeText), x, y)
        }

        // use crafting items tooltip
        if (mx in 130..153 && my in 72..87) {
            val tooltip = if (handler.useCraftingItems) {
                tooltip("redstone_assembler.use_crafting_items.true")
            } else {
                tooltip("redstone_assembler.use_crafting_items.false")
            }

            renderTooltip(matrices, tooltip, x, y)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mx = mouseX.toInt() - x
        val my = mouseY.toInt() - y

        // mode button
        if (mx in 130..153 && my in 56..71) {
            playButtonPressSound()
            if (handler.mode == RedstoneAssemblerBlockEntity.Mode.ASSEMBLER) {
                handler.mode = RedstoneAssemblerBlockEntity.Mode.CRAFTING_TABLE
            } else {
                handler.mode = RedstoneAssemblerBlockEntity.Mode.ASSEMBLER
            }
            return true
        }

        // use crafting items button
        if (mx in 130..153 && my in 72..87) {
            playButtonPressSound()
            handler.useCraftingItems = !handler.useCraftingItems
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    fun playButtonPressSound() {
        MinecraftClient.getInstance().soundManager.play(
            PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f)
        )
    }
}
