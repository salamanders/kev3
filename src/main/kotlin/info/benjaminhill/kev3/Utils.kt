import au.edu.federation.utils.Vec2f
import lejos.robotics.geometry.Rectangle2D

internal fun Vec2f.scaleUnitTo(drawingArea: Rectangle2D): Vec2f {
    require(x in 0f..1f) { "Unit Vec2f had out of bounds x:$x" }
    require(y in 0f..1f) { "Unit Vec2f had out of bounds y:$y" }
    val scaledX = drawingArea.x + (drawingArea.width * x)
    val scaledY = drawingArea.y + (drawingArea.height * y)
    return Vec2f(scaledX.toFloat(), scaledY.toFloat())
}

fun Rectangle2D.str() = "{x:$x, y:$y, w:$width, h:$height}"