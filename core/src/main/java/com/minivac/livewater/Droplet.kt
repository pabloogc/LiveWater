package com.minivac.livewater

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.utils.NumberUtils
import finnstr.libgdx.liquidfun.ParticleDef
import finnstr.libgdx.liquidfun.ParticleGroupDef
import finnstr.libgdx.liquidfun.ParticleSystem
import finnstr.libgdx.liquidfun.ParticleSystemDef
import java.nio.ByteBuffer


class Droplet {

    companion object {
        const val MAX_PARTICLES = 10000
        const val RADIUS = 1f
        const val VERTEX_PER_DROPLET = 4
        const val INDEX_PER_DROPLET = 6
        const val POTENTIAL_MAP_SIZE = 128

        val INDEX_OFFSET = shortArrayOf(0, 1, 2, 0, 2, 3)
        val ATTRIBUTES = VertexAttributes(
                VertexAttribute(Usage.Position, 3, "center"),
                VertexAttribute(Usage.Position, 3, "position")
        )

        //RENDER
        val DROPLET_BUFFER = FloatArray(ATTRIBUTES.vertexSize * MAX_PARTICLES / 4)
        val INDEX_BUFFER = ShortArray(VERTEX_PER_DROPLET * MAX_PARTICLES,
                { i ->
                    val offset = INDEX_OFFSET[i % INDEX_PER_DROPLET]
                    ((i / INDEX_PER_DROPLET) * VERTEX_PER_DROPLET + offset).toShort()
                }
        )

        val particleMesh = Mesh(
                /*isStatic*/    false,
                /*maxVertices*/ MAX_PARTICLES * VERTEX_PER_DROPLET,
                /*maxIndices*/  MAX_PARTICLES * INDEX_PER_DROPLET,
                /*attributes*/  ATTRIBUTES)
                .apply {
                    setAutoBind(false)
                    setVertices(DROPLET_BUFFER)
                    setIndices(INDEX_BUFFER)
                }

        val potentialShader = ShaderProgram(POTENTIAL_VERTEX_SHADER, POTENTIAL_FRAGMENT_SHADER).assertCompiled()
        val potentailAttributes = potentialShader.findLocations(ATTRIBUTES)
        lateinit var potentialFbo: FrameBuffer

        val normalShader = ShaderProgram(LIQUID_VERTEX_SHADER, LIQUID_FRAGMENT_SHADER).assertCompiled()
        val normalAttributes = normalShader.findLocations(ATTRIBUTES)

        //PHYSICS

        val particleSystem = ParticleSystem(Game.world, ParticleSystemDef().apply {
            radius = 0.5f
            dampingStrength = 0.2f
        })

//        val dropletParticleGroup = particleSystem.createParticleGroup(
//                ParticleGroupDef().apply {
//                    particleCount = 0
//                    flags.add(ParticleDef.ParticleType.b2_waterParticle)
//                    position.set(0f, 0f)
//                    shape = PolygonShape().apply { setAsBox(Game.WORLD_WIDTH / 10f, Game.WORLD_HEIGHT / 10f) }
//                })

        fun create() {
            potentialFbo = FrameBuffer(Pixmap.Format.RGBA8888, POTENTIAL_MAP_SIZE, POTENTIAL_MAP_SIZE, false)
            potentialFbo.colorBufferTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
            potentialFbo.colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }

        fun addParticle() {
            particleSystem.createParticle(ParticleDef().apply {
                position.set(particleSystem.particleCount / 100f, 0f)
                flags.add(ParticleDef.ParticleType.b2_waterParticle)
            })
        }

        fun render() {
            val count = pack()

            potentialFbo.begin() //Draw this to a texture
            drawPotential(count)
            potentialFbo.end()

            //Normal draw
            drawNormal(count)
        }


        private fun drawPotential(count: Int) {
            gl.glViewport(0, 0, POTENTIAL_MAP_SIZE, POTENTIAL_MAP_SIZE)

            gl.glEnable(GL20.GL_BLEND)
            gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            gl.glClearColor(0f, 0f, 0f, 1f)
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

            potentialShader.begin()
            potentialShader.setUniformMatrix("mvp", Game.camera.combined)

            particleMesh.bind(potentialShader, potentailAttributes)
            particleMesh.render(potentialShader, GL20.GL_TRIANGLES, 0, count)
            particleMesh.unbind(potentialShader)

            potentialShader.end()

            gl.glDisable(GL20.GL_BLEND)
        }

        private fun drawNormal(count: Int) {
            gl.glViewport(0, 0, app.width, app.height)

            gl.glEnable(GL20.GL_BLEND)
            gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

            normalShader.begin()
            potentialFbo.colorBufferTexture.bind(0)
            normalShader.setUniformi("tex", 0)
            normalShader.setUniformMatrix("mvp", Game.camera.combined)

            particleMesh.bind(normalShader, normalAttributes)
            particleMesh.render(normalShader, GL20.GL_TRIANGLES, 0, count)
            particleMesh.unbind(normalShader)

            normalShader.end()

            gl.glDisable(GL20.GL_BLEND)
        }

        fun pack(): Int {
            var idx = 0
            for (i in 0..particleSystem.particleCount - 1) {
                val pos = particleSystem.particlePositionBuffer[i]
                val writeVertex = fun(x: Float, y: Float) {
                    //center
                    DROPLET_BUFFER[idx++] = pos.x
                    DROPLET_BUFFER[idx++] = pos.y
                    DROPLET_BUFFER[idx++] = 0f

                    //position
                    DROPLET_BUFFER[idx++] = x
                    DROPLET_BUFFER[idx++] = y
                    DROPLET_BUFFER[idx++] = 0f
                }

                writeVertex(pos.x - RADIUS, pos.y - RADIUS)
                writeVertex(pos.x - RADIUS, pos.y + RADIUS)
                writeVertex(pos.x + RADIUS, pos.y + RADIUS)
                writeVertex(pos.x + RADIUS, pos.y - RADIUS)
            }

            particleMesh.updateVertices(0, DROPLET_BUFFER, 0, idx)

            return particleSystem.particleCount * INDEX_PER_DROPLET
        }
    }
}

//language=GLSL
private const val POTENTIAL_VERTEX_SHADER = """
    uniform mat4 mvp;

    attribute vec3 center;
    attribute vec3 position;

    varying vec3 c;
    varying vec3 p;

    void main(){
        c = center;
        p = position;
        gl_Position = mvp * vec4(position.xyz, 1.0);
    }
"""

//language=GLSL
private const val POTENTIAL_FRAGMENT_SHADER = """
    #define SQRT_2 1.414
    varying vec3 c;
    varying vec3 p;

    void main(){
        float dx = c.x - p.x;
        float dy = c.y - p.y;
        float d = (dx * dx + dy * dy);
        gl_FragColor = vec4(0.0, 0.0, 1.0, max(1.0 - d, 0.0));
    }
"""

//language=GLSL
private const val LIQUID_VERTEX_SHADER = """
    uniform mat4 mvp;
    uniform vec2 viewport;

    attribute vec3 position;
    varying vec2 t;

    void main(){
        vec4 clipPosition = mvp * vec4(position.xyz, 1.0);
        //No need to divide by w, we are using orthogonal projection
        //so its always 1

        //Map the vertex to the buffer texture that fills the screen
        t = (clipPosition.xy + vec2(1.0, 1.0)) / 2.0;
        gl_Position = clipPosition;
    }
"""

//language=GLSL
private const val LIQUID_FRAGMENT_SHADER = """
    uniform sampler2D tex;
    varying vec2 t;

    void main(){
        vec4 c = texture2D(tex, t);
        if(c.b > 0.7) gl_FragColor = vec4(0.0, 0.0, c.b, c.b);
        else discard;
    }
"""



