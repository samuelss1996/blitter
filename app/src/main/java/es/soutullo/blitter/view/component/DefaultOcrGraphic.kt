package es.soutullo.blitter.view.component

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF

/** Simple OCR graphic which causes a square to be drawn with the given bounding */
class DefaultOcrGraphic internal constructor(overlay: GraphicOverlay<OcrGraphic>, private val bounding: Rect) : OcrGraphic(overlay, null) {
    override fun contains(x: Float, y: Float): Boolean = bounding.left < x && bounding.right > x && bounding.top < y && bounding.bottom > y

    override fun draw(canvas: Canvas) {
        val rect = RectF(this.bounding)

        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)

        canvas.drawRect(rect, sRectPaint!!)
    }
}