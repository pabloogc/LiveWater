package com.minivac.livewater

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch

object Game : ApplicationAdapter() {

    lateinit var camera: Camera

    private lateinit var batch: SpriteBatch
    private lateinit var img: Texture

    override fun create() {
        batch = SpriteBatch()
        img = Texture("assets/badlogic.jpg")
        camera = OrthographicCamera(2f, 2f * Gdx.app.graphics.aspectRatio.inv())
    }

    override fun render() {
        gl.glViewport(0, 0, Gdx.app.graphics.width, Gdx.app.graphics.height)
        gl.glClearColor(0f, 0f, 0f, 1f)
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.position.set(0f, 0f, 0f)
        camera.update()

        batch.begin()
        batch.projectionMatrix = camera.combined
        batch.transformMatrix.idt().scale(1f / img.width, 1f / img.height, 1f)
        batch.draw(img, 0f, 0f)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()

        emptyList<Int>().forEachIndexed { i, p -> println("hey") }
    }
}
