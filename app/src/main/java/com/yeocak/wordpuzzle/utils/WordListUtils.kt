package com.yeocak.wordpuzzle.utils

import android.app.Application
import java.util.Locale

object WordListUtils {
	private const val WORD_LIST_NAME = "turkce_kelime_listesi.txt"

	fun isWordAvailable(word: String, application: Application): Boolean {
		if (word.length < 3) return false
		val wordSmall = word.lowercase(Locale("tr", "TR"))
		application.assets.open(WORD_LIST_NAME).apply {
			this.bufferedReader().useLines { lines ->
				lines.find { it == wordSmall }?.let {
					return true
				}
			}
		}
		return false
	}
}