package com.minivac.livewater

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.*
import finnstr.libgdx.liquidfun.*


object Game : ApplicationAdapter() {

    const val WORLD_SCALE = 4f
    const val WORLD_WIDTH = 8f * WORLD_SCALE
    const val WORLD_HEIGHT = 6f * WORLD_SCALE
    const val WORLD_DT = 1 / 60f

    val fpsLogger = FPSLogger()

    val touch = Vector3()
    val lastTouch = Vector2()
    val touchDir = Vector2()

    val world = World(Vector2(0f, -10f), true)
    lateinit var camera: OrthographicCamera
    lateinit var debugDraw: Box2DDebugRenderer
    lateinit var particleDebugDraw: ParticleDebugRenderer

    override fun create() {

        //GLProfiler.enable()

        camera = OrthographicCamera(WORLD_WIDTH * 1.3f, WORLD_HEIGHT * 1.3f)
        camera.update()

        debugDraw = Box2DDebugRenderer()

        debugDraw.SHAPE_AWAKE.set(Color.BLACK)
        debugDraw.SHAPE_STATIC.set(Color.BLACK)

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
    }


    override fun render() {
        fpsLogger.log()

        world.step(WORLD_DT, 10, 6,
                Droplet.particleSystem.calculateReasonableParticleIterations(WORLD_DT))


        val color = 0.96f
        gl.glViewport(0, 0, Gdx.app.graphics.width, Gdx.app.graphics.height)
        gl.glClearColor(color, color, color, 1f)
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        if (Gdx.input.isTouched) {

            touch.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            camera.unproject(touch)

            if (touch.x != lastTouch.x && touch.y != lastTouch.y) {
                touchDir.set(touch.x - lastTouch.x, touch.y - lastTouch.y).nor().scl(10f)
                lastTouch.set(touch.x, touch.y)
            }

            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                Game.world.gravity = touchDir
            } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                val s = CircleShape().apply { radius = WORLD_SCALE.toFloat() }
                s.position.set(touch.x, touch.y)
                val t = Transform().apply { position = Vector2(touch.x, touch.y) }
                Droplet.particleSystem.destroyParticleInShape(s, t)
            } else if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
                for (i in 1..3) {
                    Droplet.addParticle(
                            touch.x + randomSignedFloat() * WORLD_SCALE,
                            touch.y + randomSignedFloat() * WORLD_SCALE)
                }
            }
        }

        Droplet.render()
        //particleDebugDraw.render(Droplet.particleSystem, WORLD_SCALE.toFloat(), camera.combined)

        debugDraw.render(world, camera.combined)

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
