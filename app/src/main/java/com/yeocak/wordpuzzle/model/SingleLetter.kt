package com.yeocak.wordpuzzle.model

import android.os.Parcelable
import androidx.annotation.FloatRange
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class SingleLetter(
    val character: Char,
    val visualType: LetterVisualType,
    val paint: LetterPaint,
    var isSelected: Boolean,

    // Positions
    val column: Int,
    var row: RowValues,

    // Id for removing
    val id: UUID = UUID.randomUUID()
) : Parcelable {
    fun calculateNextRowValue(@FloatRange(from = 0.0, to = 1.0) addValue: Float) {
        val currentRow = row
        if (currentRow is RowValues.BetweenRows && !currentRow.isStable()) {
            when {
                currentRow.betweenRatio + addValue < 1f -> {
                    // New value is in between current values
                    currentRow.betweenRatio += addValue
                }
                currentRow.values.first == currentRow.goal || currentRow.values.second == currentRow.goal -> {
                    // Row is gonna be stable after adding value
                    row = RowValues.StableRow(currentRow.goal)
                }
                else -> {
                    // New value is in next row
                    row = RowValues.BetweenRows(
                        values = Pair(currentRow.values.first - 1, currentRow.values.second - 1),
                        betweenRatio = currentRow.betweenRatio + addValue - 1f,
                        currentRow.goal
                    )
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingleLetter

        if (character != other.character) return false
        if (visualType != other.visualType) return false
        if (column != other.column) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = character.hashCode()
        result = 31 * result + visualType.hashCode()
        result = 31 * result + column
        result = 31 * result + id.hashCode()
        return result
    }
}