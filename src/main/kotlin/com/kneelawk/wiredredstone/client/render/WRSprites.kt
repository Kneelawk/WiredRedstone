package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.WRConstants

object WRSprites {
    val RED_ALLOY_WIRE_POWERED_ID = WRConstants.id("block/red_alloy_wire_powered")
    val RED_ALLOY_WIRE_UNPOWERED_ID = WRConstants.id("block/red_alloy_wire_unpowered")
    val STANDING_RED_ALLOY_WIRE_POWERED_ID = WRConstants.id("block/standing_red_alloy_wire_powered")
    val STANDING_RED_ALLOY_WIRE_UNPOWERED_ID = WRConstants.id("block/standing_red_alloy_wire_unpowered")

    fun init() {
        // sprites in the block/ directory are now automatically registered by minecraft
    }
}
