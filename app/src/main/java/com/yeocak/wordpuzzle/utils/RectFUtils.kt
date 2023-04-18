package com.yeocak.wordpuzzle.utils

import android.graphics.RectF

fun RectF.expandAllSides(size: Float) {
	this.set(expandedAllSides(size))
}

fun RectF.expandedAllSides(size: Float): RectF {
	return RectF(this.left - size, this.top - size, this.right + size, this.bottom + size)
}