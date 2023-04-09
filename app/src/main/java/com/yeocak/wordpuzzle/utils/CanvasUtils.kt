package com.yeocak.wordpuzzle.utils

import android.graphics.Canvas
import android.graphics.Paint

fun Canvas.drawTextCentered(text: String, x: Float, y: Float, paint: Paint) {
    val xPos = x - (paint.measureText(text) / 2)
    val yPos = (y - (paint.descent() + paint.ascent()) / 2)
    drawText(text, xPos, yPos, paint)
}