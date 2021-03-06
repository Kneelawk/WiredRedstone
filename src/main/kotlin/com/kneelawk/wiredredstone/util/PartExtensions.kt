package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartContainer
import alexiil.mc.lib.multipart.api.property.MultipartPropertyContainer
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.Vec3d
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

/**
 * Checks to see if a given part has been removed.
 */
fun AbstractPart.isRemoved(): Boolean {
    return holder.container.getFirstPart { it === this } == null
}

/**
 * Gets a part from a position vector
 */
fun MultipartContainer.getSelectedPart(vec: Vec3d): AbstractPart? {
    return getFirstPart {
        for (box in it.outlineShape.boundingBoxes) {
            if (box.expand(0.01).contains(vec)) {
                return@getFirstPart true
            }
        }

        false
    }
}
