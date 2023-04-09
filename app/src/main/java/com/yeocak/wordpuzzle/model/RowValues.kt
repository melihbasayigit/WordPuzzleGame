package com.yeocak.wordpuzzle.model

import android.os.Parcelable
import androidx.annotation.FloatRange
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class RowValues(open var goal: Int) : Parcelable {
    @Parcelize
    data class StableRow(val value: Int) : RowValues(value), Parcelable

    @Parcelize
    data class BetweenRows(
        var values: Pair<Int, Int>,
        @FloatRange(from = 0.0, to = 1.0) var betweenRatio: Float,
        override var goal: Int
    ) : RowValues(goal), Parcelable

    fun isStable(): Boolean {
        return when (this) {
            is StableRow -> true
            is BetweenRows -> betweenRatio == 1f && (values.first == goal || values.second == goal)
        }
    }
}