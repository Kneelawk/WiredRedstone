package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.property.MultipartPropertyContainer
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Shortcut for [MultipartContainer.getMultipartWorld()][alexiil.mc.lib.multipart.api.MultipartContainer.getMultipartWorld]
 */
fun AbstractPart.getWorld(): World {
    return holder.container.multipartWorld
}

/**
 * Shortcut for [MultipartContainer.getMultipartBlockEntity()][alexiil.mc.lib.multipart.api.MultipartContainer.getMultipartBlockEntity]
 */
fun AbstractPart.getBlockEntity(): BlockEntity {
    return holder.container.multipartBlockEntity
}

/**
 * Shortcut for [MultipartContainer.getProperties()][alexiil.mc.lib.multipart.api.MultipartContainer.getProperties]
 */
fun AbstractPart.getProperties(): MultipartPropertyContainer {
    return holder.container.properties
}

/**
 * Shortcut for [MultipartContainer.isClientWorld()][alexiil.mc.lib.multipart.api.MultipartContainer.isClientWorld]
 */
fun AbstractPart.isClientSide(): Boolean {
    return holder.container.isClientWorld
}

/**
 * Shortcut for [MultipartContainer.redrawIfChanged()][alexiil.mc.lib.multipart.api.MultipartContainer.redrawIfChanged]
 */
fun AbstractPart.redrawIfChanged() {
    holder.container.redrawIfChanged()
}
