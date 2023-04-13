package com.yeocak.wordpuzzle.model

import android.os.Parcelable
import androidx.annotation.FloatRange
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Parcelize
sealed interface FrozenType : Parcelable {
	@Parcelize
	object NotFrozen : FrozenType, Parcelable

	@Parcelize
	data class OriginalFrozen(val affectList: MutableList<SingleLetter> = mutableListOf()) :
		FrozenType, Parcelable

	@Parcelize
	object AffectedFrozen : FrozenType, Parcelable

	companion object {
		fun getRandomInitType(
			@FloatRange(
				from = 0.0,
				to = 1.0,
				toInclusive = false
			) ratio: Float = 0.3f
		): FrozenType {
			val randomValue = Random.nextFloat()
			return if (randomValue < ratio) {
				OriginalFrozen()
			} else {
				NotFrozen
			}
		}
	}
}