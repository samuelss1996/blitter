package com.amulyakhare.textdrawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class TextDrawable private constructor(
        private val text: String,
        private val color: Int
) : Drawable() {
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        backgroundPaint.color = color
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val radius = Math.min(bounds.width(), bounds.height()) / 2f

        canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), radius, backgroundPaint)
        textPaint.textSize = radius

        val fontBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, fontBounds)
        canvas.drawText(text, bounds.exactCenterX(), bounds.exactCenterY() - fontBounds.exactCenterY(), textPaint)
    }

    override fun setAlpha(alpha: Int) {
        backgroundPaint.alpha = alpha
        textPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        backgroundPaint.colorFilter = colorFilter
        textPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    class Builder {
        fun buildRound(text: String, color: Int): TextDrawable {
            return TextDrawable(text, color)
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
