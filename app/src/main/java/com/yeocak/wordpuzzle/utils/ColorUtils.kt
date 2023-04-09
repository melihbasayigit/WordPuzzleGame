package com.yeocak.wordpuzzle.utils

import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import com.yeocak.wordpuzzle.R

enum class CustomColorSet(@ColorRes val color: Int) {
    Crystal(R.color.crystal),
    AeroBlue(R.color.areo_blue),
    Nyanza(R.color.nyanza),
    CornSilk(R.color.cornsilk),
    Bisque(R.color.bisque),
    SandyTan(R.color.sandy_tan),
    BlueCola(R.color.blue_cola),
    ButtonBlue(R.color.button_blue),
    RipeMango(R.color.ripe_mango),
    ChineseYellow(R.color.chinese_yellow),
    LavenderBlue(R.color.lavender_blue),
    BabyBlueEyes(R.color.baby_blue_eyes),
    BlizzardBlue(R.color.blizzard_blue),
    MagicMint(R.color.magic_mint),
    TeaGreen(R.color.tea_green),
}

@ColorInt
fun Resources.getColorInt(@ColorRes color: Int): Int {
    return ResourcesCompat.getColor(
        this,
        color,
        null
    )
}

@ColorRes
fun getRandomColorRes() : Int {
    return CustomColorSet.values().random().color
}

@ColorInt
fun Resources.getRandomColorInt(): Int {
    return getColorInt(getRandomColorRes())
}