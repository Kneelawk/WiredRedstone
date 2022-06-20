import java.io.File

println("Hello!")

val colors = listOf(
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple",
    "blue", "brown", "green", "red", "black"
)

val dataDir = File("../src/main/resources/data")
val cTagsDir = File(dataDir, "c/tags/items")
val wrRecipesDir = File(dataDir, "wiredredstone/recipes")

fun dye(color: String): String = """{
  "replace": false,
  "values": [
    "minecraft:${color}_dye"
  ]
}
"""

fun dyeFile(color: String): File = File(cTagsDir, "${color}_dyes.json")

fun bundledCable(color: String): String = """{
  "type": "wiredredstone:redstone_assembler_shapeless",
  "ingredients": [
    { "item": "wiredredstone:bundled_cable" },
    { "item": "minecraft:paper" },
    { "tag": "c:${color}_dyes" }
  ],
  "result": {
    "item": "wiredredstone:${color}_bundled_cable",
    "count": 1
  },
  "cookingtime": 20,
  "energypertick": 5
}
"""

fun bundledCableFile(color: String): File = File(wrRecipesDir, "${color}_bundled_cable.json")

fun bundledCableRecolor(color: String): String = """{
  "type": "wiredredstone:redstone_assembler_shapeless",
  "ingredients": [
    { "tag": "wiredredstone:colored_bundled_cables" },
    { "tag": "c:${color}_dyes" }
  ],
  "result": {
    "item": "wiredredstone:${color}_bundled_cable",
    "count": 1
  },
  "cookingtime": 10,
  "energypertick": 5
}
"""

fun bundledCableRecolorFile(color: String): File = File(wrRecipesDir, "${color}_bundled_cable_recolor.json")

fun insulatedWire(color: String): String = """{
  "type": "wiredredstone:redstone_assembler_shaped",
  "pattern": [
    "WIW",
    "WIW",
    "WIW"
  ],
  "key": {
    "W": {
      "item": "minecraft:${color}_wool"
    },
    "I": {
      "tag": "c:red_alloy_ingots"
    }
  },
  "result": {
    "item": "wiredredstone:${color}_insulated_wire",
    "count": 12
  },
  "cookingtime": 100,
  "energypertick": 5
}
"""

fun insulatedWireFile(color: String): File = File(wrRecipesDir, "${color}_insulated_wire.json")

fun insulatedWireRecolor(color: String): String = """{
  "type": "wiredredstone:redstone_assembler_shapeless",
  "ingredients": [
    { "tag": "wiredredstone:insulated_wires" },
    { "tag": "c:${color}_dyes" }
  ],
  "result": {
    "item": "wiredredstone:${color}_insulated_wire",
    "count": 1
  },
  "cookingtime": 10,
  "energypertick": 5
}
"""

fun insulatedWireRecolorFile(color: String): File = File(wrRecipesDir, "${color}_insulated_wire_recolor.json")

fun insulatedWireWrapping(color: String): String = """{
  "type": "wiredredstone:redstone_assembler_shapeless",
  "ingredients": [
    { "item": "wiredredstone:red_alloy_wire" },
    { "item": "wiredredstone:red_alloy_wire" },
    { "item": "minecraft:${color}_wool" }
  ],
  "result": {
    "item": "wiredredstone:${color}_insulated_wire",
    "count": 2
  },
  "cookingtime": 20,
  "energypertick": 5
}
"""

fun insulatedWireWrappingFile(color: String): File = File(wrRecipesDir, "${color}_insulated_wire_wrapping.json")

for (color in colors) {
    println("Writing for color: $color")

    dyeFile(color).writeText(dye(color))
    bundledCableFile(color).writeText(bundledCable(color))
    bundledCableRecolorFile(color).writeText(bundledCableRecolor(color))
    insulatedWireFile(color).writeText(insulatedWire(color))
    insulatedWireRecolorFile(color).writeText(insulatedWireRecolor(color))
    insulatedWireWrappingFile(color).writeText(insulatedWireWrapping(color))
}

println("Done.")
