package com.kneelawk.wiredredstone.partext

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRRegistries
import net.minecraft.util.registry.Registry

object WRPartExts {
    fun init() {
        Registry.register(WRRegistries.EXT_PART_TYPE, WRConstants.id("red_alloy_wire"), RedAlloyWirePartExt.Type)
    }
}