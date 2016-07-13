package com.minivac.livewater

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.VertexAttributes.Usage
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

        val INDEX_OFFSET = shortArrayOf(0, 1, 2, 0, 2, 3)
        val ATTRIBUTES = VertexAttributes(
                VertexAttribute(Usage.Position, 3, "center"),
                VertexAttribute(Usage.Position, 3, "position")
        )
        val DROPLET_BUFFER = FloatArray(ATTRIBUTES.vertexSize * MAX_PARTICLES / 4)
        val INDEX_BUFFER = ShortArray(VERTEX_PER_DROPLET * MAX_PARTICLES,
                { i ->
                    val offset = INDEX_OFFSET[i % INDEX_PER_DROPLET]
                    ((i / INDEX_PER_DROPLET) * VERTEX_PER_DROPLET + offset).toShort()
                }
        )

        val potentialShader = ShaderProgram(POTENTIAL_VERTEX_SHADER, POTENTIAL_FRAGMENT_SHADER).apply {
            if (!isCompiled) error(log)
        }

        val particleSystem = ParticleSystem(Game.world, ParticleSystemDef().apply {
            radius = RADIUS
            dampingStrength = 0.2f
        })

        val dropletParticleGroup = particleSystem.createParticleGroup(
                ParticleGroupDef().apply {
                    particleCount = 0
                    flags.add(ParticleDef.ParticleType.b2_waterParticle)
                    position.set(Game.WORLD_WIDTH / 2f, Game.WORLD_HEIGHT / 2f)
                    shape = PolygonShape().apply { setAsBox(RADIUS / 2, RADIUS / 2) }
                })

        //Data
        val particleMesh = Mesh(
                /*isStatic*/    false,
                /*maxVertices*/ MAX_PARTICLES * VERTEX_PER_DROPLET,
                /*maxIndices*/  MAX_PARTICLES * INDEX_PER_DROPLET,
                /*attributes*/  ATTRIBUTES
        ).apply {
            setVertices(DROPLET_BUFFER)
            setIndices(INDEX_BUFFER)
        }

        init {
        }

        fun render() {
            val count = pack()
            potentialShader.begin()
            potentialShader.setUniformMatrix("mvp", Game.camera.combined)
            particleMesh.render(potentialShader, GL20.GL_TRIANGLES, 0, count)
            potentialShader.end()
        }

        fun pack(): Int {
            var idx = 0
            for (i in 0..particleSystem.particleCount - 1) {
                val pos = particleSystem.particlePositionBuffer[i]
                val writeVertex = fun(x: Float, y: Float) {
                    //center, position texture
                    DROPLET_BUFFER[idx++] = pos.x
                    DROPLET_BUFFER[idx++] = pos.y
                    DROPLET_BUFFER[idx++] = 0f
                    DROPLET_BUFFER[idx++] = x
                    DROPLET_BUFFER[idx++] = y
                    DROPLET_BUFFER[idx++] = 0f
                    DROPLET_BUFFER[idx++] = x / Game.WORLD_WIDTH
                    DROPLET_BUFFER[idx++] = y / Game.WORLD_HEIGHT
                }
                writeVertex(pos.x - RADIUS, pos.y - RADIUS)
                writeVertex(pos.x - RADIUS, pos.y + RADIUS)
                writeVertex(pos.x + RADIUS, pos.y + RADIUS)
                writeVertex(pos.x + RADIUS, pos.y - RADIUS)

                println(pos)
            }


            return particleSystem.particleCount * INDEX_PER_DROPLET
        }
    }

//    init {
//        diagonal = 1f
//
//        val rx = Game.WORLD_WIDTH * 0.3
//        val ry = Game.WORLD_HEIGHT * 0.3
//
//        val cx = Game.WORLD_WIDTH / 2f
//        val cy = Game.WORLD_HEIGHT / 2f
//
//        val particleDef = ParticleDef().apply {
//            position.x = (cx - rx + 2f * rx * random.nextFloat()).toFloat()
//            position.x = (cy - ry + 2f * ry * random.nextFloat()).toFloat()
//        }
//
//        p = Game.world.createParticle(particleDef)
//        val flags = Game.world.particleFlagsBuffer[p]
//        Game.world.particleFlagsBuffer[p] = flags or 0
//
//
//        val circleShape = CircleShape()
//        circleShape.radius = diagonal
//        circleShape.radius = diagonal
//    }

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
        if(d > 1) discard;
        vec4 color = vec4(1.0, 1.0, 1.0, 1 - d);
        gl_FragColor = color;
    }
"""

private const val LIQUID_VERTEX_SHADER = """
    uniform mat4 mvp;

    attribute vec3 position;
    attribute vec2 vertexTextCoord;

    varying vec2 textCoord;

    void main(){
        textCoord = vertexTextCoord;
        gl_Position = mvp * vec4(position.xyz, 1.0);
    }
"""

private const val LIQUID_FRAGMENT_SHADER = """
    uniform sampler2D tex;
    varying vec2 textCoord;

    void main(){
        vec4 c = texture2D(tex, textCoord);
        if(c.r > 0.8) gl_FragColor = vec4(0.0, 0.0, 1.0, 0.0);
        else discard;
    }
"""



