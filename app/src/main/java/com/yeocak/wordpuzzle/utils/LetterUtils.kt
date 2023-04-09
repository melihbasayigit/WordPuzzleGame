package com.yeocak.wordpuzzle.utils

val LETTER_LIST = listOf(
    'A', 'B', 'C', 'Ç', 'D', 'E', 'F', 'G', 'Ğ', 'H', 'I', 'İ', 'J', 'K',
    'L', 'M', 'N', 'O', 'Ö', 'P', 'R', 'S', 'Ş', 'T', 'U', 'Ü', 'V', 'Y', 'Z'
)

fun getRandomLetter() = LETTER_LIST.random()

fun Char.isVowel() = this == 'A' || this == 'E' || this == 'I' || this == 'İ' ||
        this == 'O' || this == 'Ö' || this == 'U' || this == 'Ü'

enum class Letters (val point:Int){
    A(1)

    /*
        alphabet.put('A', 1)
        alphabet.put('B', 3)
        alphabet.put('C', 4)
        alphabet.put('Ç', 4)
        alphabet.put('D', 3)
        alphabet.put('E', 1)
        alphabet.put('F', 7)
        alphabet.put('G', 5)
        alphabet.put('Ğ', 8)
        alphabet.put('H', 5)
        alphabet.put('I', 2)
        alphabet.put('İ', 1)
        alphabet.put('J', 10)
        alphabet.put('K', 1)
        alphabet.put('L', 1)
        alphabet.put('M', 2)
        alphabet.put('N', 1)
        alphabet.put('O', 2)
        alphabet.put('Ö', 7)
        alphabet.put('P', 5)
        alphabet.put('R', 1)
        alphabet.put('S', 2)
        alphabet.put('Ş', 4)
        alphabet.put('T', 1)
        alphabet.put('U', 2)
        alphabet.put('Ü', 3)
        alphabet.put('V', 7)
        alphabet.put('Y', 3)
        alphabet.put('Z', 4)
    * */
}