package com.minivac.livewater

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import finnstr.libgdx.liquidfun.*


object Game : ApplicationAdapter() {

    const val WORLD_SCALE = 10
    const val WORLD_WIDTH = 8f * WORLD_SCALE
    const val WORLD_HEIGHT = 6f * WORLD_SCALE
    const val WORLD_DT = 1 / 60f

    val fpsLogger = FPSLogger()

    val world = World(Vector2(0f, -10f), true)
    lateinit var camera: OrthographicCamera
    lateinit var debugDraw: Box2DDebugRenderer
    lateinit var particleDebugDraw: ParticleDebugRenderer

    override fun create() {

        //GLProfiler.enable()

        camera = OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT)
        camera.update()

        debugDraw = Box2DDebugRenderer()
        particleDebugDraw = ParticleDebugRenderer(Color.WHITE, Droplet.MAX_PARTICLES)
        initWorld()

        Droplet.create()
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

        wallDef.position.x = WORLD_WIDTH
        wallDef.position.y = 0f
        val rightWall = world.createBody(wallDef)

        wallDef.position.x = -WORLD_WIDTH
        wallDef.position.y = 0f
        val leftWall = world.createBody(wallDef)

        wallDef.position.x = 0f
        wallDef.position.y = WORLD_HEIGHT
        val roof = world.createBody(wallDef)

        wallDef.position.x = 0f
        wallDef.position.y = -WORLD_HEIGHT
        val ground = world.createBody(wallDef)


        listOf(ground, roof, leftWall, rightWall)
                .forEach { it.createFixture(shape, 10f) }

        shape.dispose()

        for (i in 1..WORLD_SCALE * 50) {
            Droplet.addParticle()
        }
    }


    override fun render() {
        fpsLogger.log()

        world.step(Gdx.graphics.deltaTime, 10, 6,
                Droplet.particleSystem.calculateReasonableParticleIterations(Gdx.graphics.deltaTime))


        gl.glViewport(0, 0, Gdx.app.graphics.width, Gdx.app.graphics.height)
        gl.glClearColor(0.96f, 0.96f, 0.96f, 1f)
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        Droplet.render()

        //Gdx.app.log("Profile", profileString())
        //GLProfiler.reset()
    }

    override fun dispose() {
    }

    private fun profileString() = """
calls: ${GLProfiler.calls}
drawCalls: ${GLProfiler.drawCalls}
vertex: ${GLProfiler.vertexCount.count}
"""
}
