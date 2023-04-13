package com.yeocak.wordpuzzle.model

import com.yeocak.wordpuzzle.utils.Letters
import com.yeocak.wordpuzzle.utils.isVowel

enum class LetterVisualType {
    RECT, CIRCLE;

    companion object {
        fun getTypeOfChar(letter: Char) = if (letter.isVowel()) CIRCLE else RECT
        fun getTypeOfLetter(letter: Letters) = if (letter.isVowel) CIRCLE else RECT
    }
}