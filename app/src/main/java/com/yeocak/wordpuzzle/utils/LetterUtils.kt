package com.yeocak.wordpuzzle.utils

val LETTER_LIST = listOf(
    'A', 'B', 'C', 'Ç', 'D', 'E', 'F', 'G', 'Ğ', 'H', 'I', 'İ', 'J', 'K',
    'L', 'M', 'N', 'O', 'Ö', 'P', 'R', 'S', 'Ş', 'T', 'U', 'Ü', 'V', 'Y', 'Z'
)

fun getRandomLetter() = LETTER_LIST.random()

fun Char.isVowel() = this == 'A' || this == 'E' || this == 'I' || this == 'İ' ||
        this == 'O' || this == 'Ö' || this == 'U' || this == 'Ü'