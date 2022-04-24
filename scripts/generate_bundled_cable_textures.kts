import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

println("Hello!")

val blockDir = File("../src/main/resources/assets/wiredredstone/textures/block")
val bcDir = File(blockDir, "bundled_cable")
val iwDir = File(blockDir, "insulated_wire")
val whiteDir = File(bcDir, "white")
val baseTopCross = ImageIO.read(File(whiteDir, "top_cross.png"))
val baseTopX = ImageIO.read(File(whiteDir, "top_x.png"))
val baseTopZ = ImageIO.read(File(whiteDir, "top_z.png"))
val baseLowerSide = ImageIO.read(File(whiteDir, "lower_side.png"))
val baseUpperSide = ImageIO.read(File(whiteDir, "upper_side.png"))
val baseBottomCross = ImageIO.read(File(whiteDir, "bottom_cross.png"))
val baseBottomX = ImageIO.read(File(whiteDir, "bottom_x.png"))
val baseBottomZ = ImageIO.read(File(whiteDir, "bottom_z.png"))

println("Base images loaded.")

val otherColors = listOf("orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black")

fun Graphics2D.drawCross(light: Int, medium: Int, dark: Int) {
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

fun Graphics2D.drawX(light: Int, medium: Int, dark: Int) {
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

fun Graphics2D.drawZ(light: Int, medium: Int, dark: Int) {
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

fun Graphics2D.drawSide(light: Int, medium: Int, dark: Int) {
    setColor(Color(light))
    fillRect(2, 5, 1, 4)
    fillRect(12, 5, 1, 4)

    setColor(Color(dark))
    fillRect(3, 5, 1, 4)
    fillRect(13, 5, 1, 4)

    setColor(Color(medium))
    fillRect(3, 6, 1, 2)
    fillRect(13, 7, 1, 1)
}

for (color in otherColors) {
    println("Starting on color: $color...")

    val colorDir = File(bcDir, color)
    val colorCross = ImageIO.read(File(iwDir, "${color}_top_cross.png"))
    val light = colorCross.getRGB(7, 6)
    val medium = colorCross.getRGB(7, 7)
    val dark = colorCross.getRGB(8, 7)

    println("  Drawing top cross image...")
    val outTopCross = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    with(outTopCross.createGraphics()) {
        drawImage(baseTopCross, 0, 0, null)
        drawCross(light, medium, dark)
    }
    println("  Writing top cross image...")
    ImageIO.write(outTopCross, "png", File(colorDir, "top_cross.png"))

    println("  Drawing top x image...")
    val outTopX = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    with(outTopX.createGraphics()) {
        drawImage(baseTopX, 0, 0, null)
        drawX(light, medium, dark)
    }
    println("  Writing top x image...")
    ImageIO.write(outTopX, "png", File(colorDir, "top_x.png"))

    println("  Drawing top z image...")
    val outTopZ = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    with(outTopZ.createGraphics()) {
        drawImage(baseTopZ, 0, 0, null)
        drawZ(light, medium, dark)
    }
    println("  Writing top z image...")
    ImageIO.write(outTopZ, "png", File(colorDir, "top_z.png"))

    println("  Drawing lower side image...")
    val outLowerSide = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    with(outLowerSide.createGraphics()) {
        drawImage(baseLowerSide, 0, 0, null)
        drawSide(light, medium, dark)
    }
    println("  Writing lower side image...")
    ImageIO.write(outLowerSide, "png", File(colorDir, "lower_side.png"))

    println("  Drawing upper side image...")
    val outUpperSide = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    with(outUpperSide.createGraphics()) {
        drawImage(baseUpperSide, 0, 0, null)
        drawSide(light, medium, dark)
    }
    println("  Writing upper side image...")
    ImageIO.write(outUpperSide, "png", File(colorDir, "upper_side.png"))

    println("  Drawing bottom cross image...")
    val outBottomCross = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    with(outBottomCross.createGraphics()) {
        drawImage(baseBottomCross, 0, 0, null)
        drawCross(light, medium, dark)
    }
    println("  Writing bottom cross image...")
    ImageIO.write(outBottomCross, "png", File(colorDir, "bottom_cross.png"))

    println("  Drawing bottom x image...")
    val outBottomX = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    with(outBottomX.createGraphics()) {
        drawImage(baseBottomX, 0, 0, null)
        drawX(light, medium, dark)
    }
    println("  Writing bottom x image...")
    ImageIO.write(outBottomX, "png", File(colorDir, "bottom_x.png"))

    println("  Drawing bottom z image...")
    val outBottomZ = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    with(outBottomZ.createGraphics()) {
        drawImage(baseBottomZ, 0, 0, null)
        drawZ(light, medium, dark)
    }
    println("  Writing bottom z image...")
    ImageIO.write(outBottomZ, "png", File(colorDir, "bottom_z.png"))

    println()
}

println("Done.")
