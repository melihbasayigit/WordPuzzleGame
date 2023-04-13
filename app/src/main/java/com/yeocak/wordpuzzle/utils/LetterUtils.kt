package com.yeocak.wordpuzzle.utils

import androidx.annotation.FloatRange
import kotlin.random.Random

fun Char.isVowel() = this == 'A' || this == 'E' || this == 'I' || this == 'İ' ||
		this == 'O' || this == 'Ö' || this == 'U' || this == 'Ü'

enum class Letters(val point: Int, val isVowel: Boolean) {
	A(1, true),
	B(3, false),
	C(4, false),
	Ç(4, false),
	D(3, false),
	E(1, true),
	F(7, false),
	G(5, false),
	Ğ(8, false),
	H(5, false),
	I(2, true),
	İ(1, true),
	J(10, false),
	K(1, false),
	L(1, false),
	M(2, false),
	N(1, false),
	O(2, true),
	Ö(7, true),
	P(5, false),
	R(1, false),
	S(2, false),
	Ş(4, false),
	T(1, false),
	U(2, true),
	Ü(3, true),
	V(7, false),
	Y(3, false),
	Z(4, false);

	val char: Char
		get() = this.name.first()

	companion object {
		fun getVowelList() = Letters.values().filter { letter ->
			letter.isVowel
		}

		fun getConsonantList() = Letters.values().filter { letter ->
			!letter.isVowel
		}

		fun getRandomLetterByRatio(
			@FloatRange(
				from = 0.0,
				to = 1.0,
				toInclusive = false
			) vowelRatio: Float = 0.3f
		): Letters {
			val randomValue = Random.nextFloat()
			return if (randomValue < vowelRatio) {
				getVowelList().random()
			} else {
				getConsonantList().random()
			}
		}
	}
}