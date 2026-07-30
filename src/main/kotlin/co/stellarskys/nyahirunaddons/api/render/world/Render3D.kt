package co.stellarskys.nyahirunaddons.api.render.world

import co.stellarskys.nyahirunaddons.NyahirunAddons.partialTicks
import co.stellarskys.stella.api.zenith.Zenith.player
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f
import java.awt.Color

object Render3D {
    private val BOX_EDGES = arrayOf(
        intArrayOf(0, 1), intArrayOf(0, 2), intArrayOf(1, 3), intArrayOf(2, 3),
        intArrayOf(4, 5), intArrayOf(4, 6), intArrayOf(5, 7), intArrayOf(6, 7),
        intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7),
    )

    private val BOX_FACES = arrayOf(
        intArrayOf(0, 4, 5, 1),
        intArrayOf(2, 3, 7, 6),
        intArrayOf(0, 2, 6, 4),
        intArrayOf(1, 5, 7, 3),
        intArrayOf(0, 1, 3, 2),
        intArrayOf(4, 6, 7, 5)
    )

    /* =========================
       getCameraPos
       ========================= */

    fun getCameraPos(): Vec3 {
        return Minecraft.getInstance()
            .gameRenderer
            .mainCamera
            .position()
    }

    /* =========================
       drawSingleLineToVertex
       ========================= */

    fun drawSingleLineToVertex(
        ctx: RenderContext,
        start: Vec3,
        end: Vec3,
        r: Float, g: Float, b: Float, a: Float,
        withOffset: Boolean
    ) {
        val layer: RenderType = ApiRenderLayers.LINES_DEPTH
        val buffer = ctx.consumers.getBuffer(layer)
        val posMatrix: Matrix4f = ctx.matrixStack.last().pose()

        val endPos = if (withOffset) end.add(getCameraPos().reverse()) else end

        val x1 = start.x.toFloat()
        val y1 = start.y.toFloat()
        val z1 = start.z.toFloat()

        val x2 = endPos.x.toFloat()
        val y2 = endPos.y.toFloat()
        val z2 = endPos.z.toFloat()

        val normal = Vector3f(
            x2 - x1,
            y2 - y1,
            z2 - z1
        ).normalize()

        buffer.addVertex(posMatrix, x1, y1, z1)
            .setColor(r, g, b, a)
            .setNormal(normal.x(), normal.y(), normal.z())
            .setLineWidth(3.0f)

        buffer.addVertex(posMatrix, x2, y2, z2)
            .setColor(r, g, b, a)
            .setNormal(normal.x(), normal.y(), normal.z())
            .setLineWidth(3.0f)
    }

    /* =========================
       drawSingleLine
       ========================= */

    fun drawSingleLine(
        ctx: RenderContext,
        start: Vec3,
        end: Vec3,
        color: Color,
        depthtest: Boolean
    ) {
        val layer = if (depthtest) ApiRenderLayers.LINES_DEPTH else ApiRenderLayers.LINES
        val buffer = ctx.consumers.getBuffer(layer)
        val posMatrix = ctx.matrixStack.last().pose()

        val x1 = start.x.toFloat()
        val y1 = start.y.toFloat()
        val z1 = start.z.toFloat()

        val x2 = end.x.toFloat()
        val y2 = end.y.toFloat()
        val z2 = end.z.toFloat()

        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        val normal = Vector3f(
            x2 - x1,
            y2 - y1,
            z2 - z1
        ).normalize()

        buffer.addVertex(posMatrix, x1, y1, z1)
            .setColor(red, green, blue, alpha)
            .setNormal(normal.x(), normal.y(), normal.z())
            .setLineWidth(3.0f)

        buffer.addVertex(posMatrix, x2, y2, z2)
            .setColor(red, green, blue, alpha)
            .setNormal(normal.x(), normal.y(), normal.z())
            .setLineWidth(3.0f)
    }

    /* =========================
       drawQuadfill
       ========================= */

    fun drawQuadfill(
        ctx: RenderContext,
        a: Vec3, b: Vec3, c: Vec3, d: Vec3,
        color: Color,
        depthtest: Boolean
    ) {
        val fillLayer = if (depthtest) ApiRenderLayers.FILLED_DEPTH else ApiRenderLayers.FILLED
        val buf = ctx.consumers.getBuffer(fillLayer)
        val mat = ctx.matrixStack.last().pose()

        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        vertex(buf, mat, a, red, green, blue, alpha)
        vertex(buf, mat, b, red, green, blue, alpha)
        vertex(buf, mat, c, red, green, blue, alpha)
        vertex(buf, mat, d, red, green, blue, alpha)
    }

    fun drawBox(
        ctx: RenderContext,
        box: AABB,
        lineColor: Color? = null,
        fillColor: Color? = null,
        depthtest: Boolean = false
    ) {
        if (lineColor == null && fillColor == null) return

        val vertices = getBoxVertices(box, getCameraPos())

        lineColor?.let { color ->
            for (edge in BOX_EDGES) {
                drawSingleLine(
                    ctx,
                    vertices[edge[0]],
                    vertices[edge[1]],
                    color,
                    depthtest
                )
            }
        }

        fillColor?.let { color ->
            for (face in BOX_FACES) {
                drawQuadfill(
                    ctx,
                    vertices[face[0]],
                    vertices[face[1]],
                    vertices[face[2]],
                    vertices[face[3]],
                    color,
                    depthtest
                )
            }
        }
    }

    private fun vertex(
        buf: VertexConsumer,
        mat: Matrix4f,
        v: Vec3,
        r: Float, g: Float, b: Float, a: Float
    ) {
        buf.addVertex(mat, v.x.toFloat(), v.y.toFloat(), v.z.toFloat())
            .setColor(r, g, b, a)
    }

    /* =========================
       getLookVec
       ========================= */

    fun getLookVec(): Vec3 {
        val player = player ?: return Vec3.ZERO

        val pitch = player.getViewXRot(partialTicks)
        val yaw = player.getViewYRot(partialTicks)

        return Rotation(pitch, yaw).asLookVec()
    }

    /* =========================
       getLerpedBoxForBox
       ========================= */

    fun getLerpedBoxForBox(e: Entity): AABB {
        val x = Mth.lerp(partialTicks.toDouble(), e.xOld, e.x)
        val y = Mth.lerp(partialTicks.toDouble(), e.yOld, e.y)
        val z = Mth.lerp(partialTicks.toDouble(), e.zOld, e.z)

        val ox = x - e.x
        val oy = y - e.y
        val oz = z - e.z

        return e.boundingBox.move(ox, oy, oz)
    }

    /* =========================
       getBoxVertices
       ========================= */

    fun getBoxVertices(box: AABB, cam: Vec3): Array<Vec3> {
        return arrayOf(
            Vec3(box.minX, box.minY, box.minZ).subtract(cam),
            Vec3(box.minX, box.minY, box.maxZ).subtract(cam),
            Vec3(box.minX, box.maxY, box.minZ).subtract(cam),
            Vec3(box.minX, box.maxY, box.maxZ).subtract(cam),
            Vec3(box.maxX, box.minY, box.minZ).subtract(cam),
            Vec3(box.maxX, box.minY, box.maxZ).subtract(cam),
            Vec3(box.maxX, box.maxY, box.minZ).subtract(cam),
            Vec3(box.maxX, box.maxY, box.maxZ).subtract(cam)
        )
    }
}
