import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

println("Hello!")

val blockDir = File("../src/main/resources/assets/wiredredstone/textures/block")
val bcDir = File(blockDir, "standing_bundled_cable")
val iwDir = File(blockDir, "insulated_wire")
val whiteDir = File(bcDir, "white")
val baseTopCross = ImageIO.read(File(whiteDir, "top_cross.png"))
val baseTopX = ImageIO.read(File(whiteDir, "top_x.png"))
val baseTopZ = ImageIO.read(File(whiteDir, "top_z.png"))
val baseLowerCross = ImageIO.read(File(whiteDir, "lower_cross.png"))
val baseLowerX = ImageIO.read(File(whiteDir, "lower_x.png"))
val baseLowerZ = ImageIO.read(File(whiteDir, "lower_z.png"))
val baseUpperCross = ImageIO.read(File(whiteDir, "upper_cross.png"))
val baseUpperX = ImageIO.read(File(whiteDir, "upper_x.png"))
val baseUpperZ = ImageIO.read(File(whiteDir, "upper_z.png"))
val baseBottomCross = ImageIO.read(File(whiteDir, "bottom_cross.png"))
val baseBottomX = ImageIO.read(File(whiteDir, "bottom_x.png"))
val baseBottomZ = ImageIO.read(File(whiteDir, "bottom_z.png"))

println("Base images loaded.")

val otherColors = listOf(
    "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue",
    "brown", "green", "red", "black"
)

fun drawCross(graphics: Graphics2D, light: Int, medium: Int, dark: Int) {
    with(graphics) {
        setColor(Color(light))
        fillRect(5, 2, 6, 1)
        fillRect(2, 5, 1, 6)
        fillRect(12, 5, 1, 6)
        fillRect(5, 12, 6, 1)
        fillRect(7, 7, 1, 1)

        setColor(Color(dark))
        fillRect(5, 3, 6, 1)
        fillRect(3, 5, 1, 6)
        fillRect(13, 5, 1, 6)
        fillRect(5, 13, 6, 1)
        fillRect(8, 8, 1, 1)

        setColor(Color(medium))
        fillRect(6, 3, 1, 1)
        fillRect(8, 3, 1, 1)
        fillRect(3, 7, 1, 1)
        fillRect(3, 9, 1, 1)
        fillRect(13, 6, 1, 1)
        fillRect(13, 9, 1, 1)
        fillRect(6, 13, 1, 1)
        fillRect(9, 13, 1, 1)
        fillRect(8, 7, 1, 1)
        fillRect(7, 8, 1, 1)
    }
}

fun drawX(graphics: Graphics2D, light: Int, medium: Int, dark: Int) {
    with(graphics) {
        setColor(Color(light))
        fillRect(2, 5, 1, 6)
        fillRect(12, 5, 1, 6)
        fillRect(7, 7, 1, 1)

        setColor(Color(dark))
        fillRect(3, 5, 1, 6)
        fillRect(13, 5, 1, 6)
        fillRect(8, 8, 1, 1)

        setColor(Color(medium))
        fillRect(3, 7, 1, 1)
        fillRect(3, 9, 1, 1)
        fillRect(13, 6, 1, 1)
        fillRect(13, 9, 1, 1)
        fillRect(8, 7, 1, 1)
        fillRect(7, 8, 1, 1)
    }
}

fun drawZ(graphics: Graphics2D, light: Int, medium: Int, dark: Int) {
    with(graphics) {
        setColor(Color(light))
        fillRect(5, 2, 6, 1)
        fillRect(5, 12, 6, 1)
        fillRect(7, 7, 1, 1)

        setColor(Color(dark))
        fillRect(5, 3, 6, 1)
        fillRect(5, 13, 6, 1)
        fillRect(8, 8, 1, 1)

        setColor(Color(medium))
        fillRect(6, 3, 1, 1)
        fillRect(8, 3, 1, 1)
        fillRect(6, 13, 1, 1)
        fillRect(9, 13, 1, 1)
        fillRect(8, 7, 1, 1)
        fillRect(7, 8, 1, 1)
    }
}

for (color in otherColors) {
    println("Starting on color: $color...")

    val colorDir = File(bcDir, color)
    if (!colorDir.exists()) colorDir.mkdirs()
    val colorCross = ImageIO.read(File(iwDir, "${color}_top_cross.png"))
    val light = colorCross.getRGB(7, 6)
    val medium = colorCross.getRGB(7, 7)
    val dark = colorCross.getRGB(8, 7)

    fun makeImage(
        display: String, file: String, base: BufferedImage, draw: (Graphics2D, Int, Int, Int) -> Unit
    ) {
        println("  Drawing $display image...")
        val outLowerCross = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        with(outLowerCross.createGraphics()) {
            drawImage(base, 0, 0, null)
            draw(this, light, medium, dark)
        }
        println("  Writing $display image...")
        ImageIO.write(outLowerCross, "png", File(colorDir, "$file.png"))
    }

    makeImage("top cross", "top_cross", baseTopCross, ::drawCross)
    makeImage("top x", "top_x", baseTopX, ::drawX)
    makeImage("top z", "top_z", baseTopZ, ::drawZ)

    makeImage("lower cross", "lower_cross", baseLowerCross, ::drawCross)
    makeImage("lower x", "lower_x", baseLowerX, ::drawX)
    makeImage("lower z", "lower_z", baseLowerZ, ::drawZ)

    makeImage("upper cross", "upper_cross", baseUpperCross, ::drawCross)
    makeImage("upper x", "upper_x", baseUpperX, ::drawX)
    makeImage("upper z", "upper_z", baseUpperZ, ::drawZ)

    makeImage("bottom cross", "bottom_cross", baseBottomCross, ::drawCross)
    makeImage("bottom x", "bottom_x", baseBottomX, ::drawX)
    makeImage("bottom z", "bottom_z", baseBottomZ, ::drawZ)

    println()
}

println("Done.")
