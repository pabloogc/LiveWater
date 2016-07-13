package com.minivac.livewater

import com.badlogic.gdx.Graphics
import com.badlogic.gdx.utils.NumberUtils


val Graphics.aspectRatio: Float
    get() = width.toFloat() / height

fun Float.inv(): Float = 1f / this


//Float Array utilities

//Int utilities
val Int.floatBits: Float get() = NumberUtils.intBitsToFloat(this)
val Int.floatColorBits: Float get() = NumberUtils.intToFloatColor(this)
val Int.K: Int get() = this * 1000
val Int.M: Int get() = this.K.K