package com.minivac.livewater

import com.badlogic.gdx.Graphics


val Graphics.aspectRatio: Float
    get() = width.toFloat() / height

fun Float.inv(): Float = 1f / this