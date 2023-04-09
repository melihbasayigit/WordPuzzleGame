package com.yeocak.wordpuzzle.model

import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.min

class PuzzleField(val columnNumber: Int, val rowNumber: Int) {

    var width: Float = 0f
        private set
    var height: Float = 0f
        private set

    var horizontalPadding = 0f
    var verticalPadding = 0f

    val boundRatio: Float
        get() {
            return rowNumber.toFloat() / columnNumber
        }

    var letterSize: Float = 0f
        private set

    var fontSize: Float = 0f

    var fieldCoordinates: RectF = RectF(0f, 0f, 0f, 0f)
        private set

    fun setBounds(viewWidth: Int, viewHeight: Int) {
        // Set width and height
        val viewRatio = viewHeight.toFloat() / viewWidth

        if (viewRatio > boundRatio) {
            width = viewWidth.toFloat()
            height = width * boundRatio
        } else {
            height = viewHeight.toFloat()
            width = height / boundRatio
        }

        // Set coordinates
        horizontalPadding = (viewWidth - width) / 2
        verticalPadding = (viewHeight - height) / 2

        fieldCoordinates = RectF(
            horizontalPadding,
            verticalPadding,
            horizontalPadding + width,
            verticalPadding + height
        )

        // Set letter size
        val singleLetterWidth = (width - (LETTER_SPACING * (columnNumber - 1))) / columnNumber
        val singleLetterHeight = (height - (LETTER_SPACING * (rowNumber - 1))) / rowNumber
        letterSize = min(singleLetterWidth, singleLetterHeight)

        // Set font size
        fontSize = letterSize / 2
    }

    fun getLetterCenter(row: RowValues, column: Int): PointF {
        val letterRadius = letterSize / 2
        val posX = (letterSize + LETTER_SPACING) * column + letterRadius + horizontalPadding

        return when (row) {
            is RowValues.StableRow -> {
                val posY =
                    height - ((letterSize + LETTER_SPACING) * row.value + letterRadius) + verticalPadding
                PointF(posX, posY)
            }
            is RowValues.BetweenRows -> {
                val topY =
                    height - ((letterSize + LETTER_SPACING) * row.values.second + letterRadius) + verticalPadding
                val bottomY =
                    height - ((letterSize + LETTER_SPACING) * row.values.first + letterRadius) + verticalPadding
                val differenceByRatio = (bottomY - topY) * row.betweenRatio

                PointF(posX, topY + differenceByRatio)
            }
        }
    }

    fun getLetterCoordinates(row: RowValues, column: Int): RectF {
        val center = getLetterCenter(row, column)
        val radius = letterSize / 2
        return RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius,
        )
    }

    fun getLetterTableLocation(point: PointF): Pair<Int, Int> {
        val singleBoxSize = letterSize + LETTER_SPACING
        val column = (point.x - horizontalPadding) / singleBoxSize
        val row = (point.y - verticalPadding) / singleBoxSize
        return Pair(row.toInt(), column.toInt())
    }

    companion object {
        const val LETTER_SPACING = 16
    }
}