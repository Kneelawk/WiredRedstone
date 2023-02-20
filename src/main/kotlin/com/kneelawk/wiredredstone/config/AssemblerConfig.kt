package com.kneelawk.wiredredstone.config

import org.quiltmc.config.api.Config
import org.quiltmc.config.api.annotations.Comment

data class AssemblerConfig(
    @Comment("The maximum energy that a Redstone Assembler can hold.")
    @Comment("By default, 1 Coal generates 4000 energy.")
    @Comment("This value is synced to the client.")
    val energyCapacity: Long = 128000L,

    @Comment("The maximum amount of energy that can be inserted per tick.")
    val maxInsert: Long = 128L,

    @Comment("The amount of time, in ticks, it takes to complete a vanilla crafting recipe.")
    val craftingTime: Int = 10,

    @Comment("The amount of energy consumed per tick when performing vanilla crafting.")
    val craftingEnergy: Int = 5,

    @Comment("The amount of energy generated per tick by burning something.")
    val burnEnergy: Int = 5,

    @Comment("The amount by which furnace fuel burn times are multiplied to obtain Redstone Assembler fuel burn times.")
    val burnTimeFactor: Float = 0.5f
) : Config.Section {
    companion object {
        val instance: AssemblerConfig
            get() = CommonConfig.current.assembler
    }
}
