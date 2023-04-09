package com.yeocak.wordpuzzle.model

import android.graphics.Paint
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class LetterPaint(
    @ColorInt val color: Int
) : Parcelable {
    @IgnoredOnParcel
    val unselectedBackground = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        color = this@LetterPaint.color
        strokeWidth = 4f
    }

    @IgnoredOnParcel
    val selectedBackground = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = this@LetterPaint.color
        strokeWidth = 4f
    }

    @IgnoredOnParcel
    val selectedText = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = this@LetterPaint.color
        textSize = this@LetterPaint.textSize
    }

    @IgnoredOnParcel
    var textSize: Float = 0f
        set(value) {
            field = value
            selectedText.textSize = value
        }

    constructor(@ColorInt color: Int, textSize: Float) : this(color) {
        this.textSize = textSize
    }
}