package com.yeocak.wordpuzzle.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun Int.decodeToBitmap(resources: Resources): Bitmap? {
	return BitmapFactory.decodeResource(resources, this)
}