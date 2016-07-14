package com.minivac.livewater

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import finnstr.libgdx.liquidfun.ParticleDef
import finnstr.libgdx.liquidfun.ParticleGroupDef
import finnstr.libgdx.liquidfun.ParticleSystem
import finnstr.libgdx.liquidfun.ParticleSystemDef


object Game : ApplicationAdapter() {

    const val WORLD_SCALE = 1
    const val WORLD_WIDTH = 8f * WORLD_SCALE
    const val WORLD_HEIGHT = 6f * WORLD_SCALE
    const val WORLD_DT = 1 / 60f

    val world = World(Vector2(0f, -10f), false)
    lateinit var camera: OrthographicCamera
    lateinit var debugDraw: Box2DDebugRenderer


    override fun create() {

        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        camera = OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT)
        camera.update()

        debugDraw = Box2DDebugRenderer()
        initWorld()
    }

    private fun initWorld() {
        val wallDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(0f, 0f)
        }

        val shape = PolygonShape()
        val boxHalfWidth = WORLD_WIDTH / 2
        val boxHalfHeight = WORLD_HEIGHT / 2
        shape.setAsBox(boxHalfWidth, boxHalfHeight)

        wallDef.position.x = 4f
        wallDef.position.y = -boxHalfHeight
        val ground = world.createBody(wallDef)

//        wallDef.position.y = WORLD_HEIGHT + boxHalfHeight
//        val roof = world.createBody(wallDef)
//
//        wallDef.position.x = -boxHalfWidth
//        wallDef.position.y = WORLD_HEIGHT / 2
//        val leftWall = world.createBody(wallDef)
//
//        wallDef.position.x = WORLD_WIDTH + boxHalfWidth
//        val rightWall = world.createBody(wallDef)

        listOf(ground)
                .forEach { it.createFixture(shape, 10f) }

    }


    override fun render() {
        world.step(WORLD_DT, 10, 6, 2)

        gl.glViewport(0, 0, Gdx.app.graphics.width, Gdx.app.graphics.height)

        gl.glClearColor(0f, 0f, 0f, 1f)
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        Droplet.render()

        debugDraw.render(world, camera.combined)

    }

    override fun dispose() {
    }
}
