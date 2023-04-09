package com.yeocak.wordpuzzle.model

import com.yeocak.wordpuzzle.utils.isVowel

enum class LetterVisualType {
    RECT, CIRCLE;

    companion object {
        fun getTypeOfLetter(letter: Char) = if (letter.isVowel()) CIRCLE else RECT
    }
}