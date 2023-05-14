package com.kneelawk.wiredredstone.compat.rei.impl

import com.kneelawk.wiredredstone.WRConstants
import me.shedaniel.rei.api.common.category.CategoryIdentifier

object WiredRedstoneREI {
    val REDSTONE_ASSEMBLER_CATEGORY: CategoryIdentifier<RedstoneAssemblerDisplay> =
        CategoryIdentifier.of(WRConstants.id("redstone_assembler"))
}
