package com.azamovhudstc.playstoretodoapp.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import java.util.*

internal class CircleDrawable(val list: ArrayList<Int>) : Drawable() {

    private val mPaint: Paint = Paint()

    override fun draw(canvas: Canvas) {
        val b = bounds
        // val lvl = level
        // val xx = b.width() * lvl / 10000.0f
        val yy = (b.height() - mPaint.strokeWidth) / 2
        val size = if (list.size > 9) 8 else list.size
        for (i in 1..size) {
            mPaint.color = list[i - 1]
            val y = if (i < 5) yy else yy + 20f
            val x = getPositionX(i)
            canvas.drawCircle(x, y, 8f, mPaint)
        }
    }

    override fun onLevelChange(level: Int): Boolean {
        invalidateSelf()
        return true
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: ColorFilter?) {}
    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    init {
        mPaint.strokeWidth = 10f
    }

    private fun getPositionX(i: Int): Float {
        return when (i) {
            1 -> 8f
            2 -> 28f
            3 -> 48f
            4 -> 68f
            5 -> 8f
            6 -> 28f
            7 -> 48f
            8 -> 68f
            else -> -8f
        }
    }
}