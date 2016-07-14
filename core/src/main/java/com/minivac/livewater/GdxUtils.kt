package com.minivac.livewater

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.NumberUtils


private val tmpVec2 = FloatArray(2)
val Application.viewport: FloatArray
    get() {
        tmpVec2[0] = Gdx.app.graphics.width.toFloat()
        tmpVec2[1] = Gdx.app.graphics.height.toFloat()
        return com.minivac.livewater.tmpVec2
    }

val Application.width: Int
    get() = Gdx.app.graphics.width

val Application.height: Int
    get() = Gdx.app.graphics.height

val Graphics.aspectRatio: Float
    get() = width.toFloat() / height

fun Float.inv(): Float = 1f / this


//Shaders
fun ShaderProgram.assertCompiled() = apply { if (!isCompiled) error("Shader compilation failed:\n" + log) }

fun ShaderProgram.findLocation(attr: VertexAttribute) = getAttributeLocation(attr.alias)
fun ShaderProgram.findLocations(vararg attrs: VertexAttribute) = attrs.map { findLocation(it) }.toIntArray()
fun ShaderProgram.findLocations(attrs: VertexAttributes) = attrs.map { findLocation(it) }.toIntArray()

//Float Array utilities

//Int utilities
val Int.floatBits: Float get() = NumberUtils.intBitsToFloat(this)
val Int.floatColorBits: Float get() = NumberUtils.intToFloatColor(this)
val Int.K: Int get() = this * 1000
val Int.M: Int get() = this.K.K

//Time utilities
inline fun timed(name: String, f: () -> Unit) {
    val start = System.nanoTime()
    f()
    val elapsed = (System.nanoTime() - start) / 1000000
    Gdx.app.log("Time", "$name: $elapsed ms")
}

//Random utilities
fun randomSignedFloat() = -1f + 2f * Math.random().toFloat()
