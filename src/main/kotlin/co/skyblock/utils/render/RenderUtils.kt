package co.skyblock.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import co.skyblock.utils.render.mixinterface.IMinecraftClient
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f
import java.awt.Color

object RenderUtils {

    /* =========================
       getCameraPos
       ========================= */

    fun getCameraPos(): Vec3 {
        return Minecraft.getInstance()
            .gameRenderer
            .mainCamera
            .position
    }

    /* =========================
       drawSingleLineToVertex
       ========================= */

    fun drawSingleLineToVertex(
        matrixStack: PoseStack,
        start: Vec3,
        end: Vec3,
        r: Float, g: Float, b: Float, a: Float,
        withOffset: Boolean
    ) {
        val vcp = getVertexConsumerProvider()
        val layer: RenderType = GemRenderLayers.ESP_LINES
        val buffer = vcp.getBuffer(layer)
        val posMatrix: Matrix4f = matrixStack.last().pose()

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

        buffer.addVertex(posMatrix, x2, y2, z2)
            .setColor(r, g, b, a)
            .setNormal(normal.x(), normal.y(), normal.z())

        vcp.endBatch(layer)
    }

    /* =========================
       drawSingleLine
       ========================= */

    fun drawSingleLine(
        matrixStack: PoseStack,
        start: Vec3,
        end: Vec3,
        color: Color,
        depthtest: Boolean
    ) {
        val vcp = getVertexConsumerProvider()
        val layer = if (depthtest) GemRenderLayers.ESP_LINES else GemRenderLayers.LINES
        val buffer = vcp.getBuffer(layer)
        val posMatrix = matrixStack.last().pose()

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

        buffer.addVertex(posMatrix, x2, y2, z2)
            .setColor(red, green, blue, alpha)
            .setNormal(normal.x(), normal.y(), normal.z())

        vcp.endBatch(layer)
    }

    /* =========================
       drawQuadfill
       ========================= */

    fun drawQuadfill(
        matrices: PoseStack,
        a: Vec3, b: Vec3, c: Vec3, d: Vec3,
        color: Color,
        depthtest: Boolean
    ) {
        val vcp = getVertexConsumerProvider()
        val fillLayer = if (depthtest) GemRenderLayers.ESP_FILLS else GemRenderLayers.FILLS
        val buf = vcp.getBuffer(fillLayer)
        val mat = matrices.last().pose()

        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        vertex(buf, mat, a, red, green, blue, alpha)
        vertex(buf, mat, b, red, green, blue, alpha)
        vertex(buf, mat, c, red, green, blue, alpha)

        vertex(buf, mat, d, red, green, blue, alpha)
        vertex(buf, mat, c, red, green, blue, alpha)
        vertex(buf, mat, b, red, green, blue, alpha)

        vcp.endBatch(fillLayer)
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
       getLerpedBox
       ========================= */

    fun getLerpedBox(e: Entity, delta: Float): AABB {
        val offset = MathUtils.lerp(
            delta,
            e.position(),
            Vec3(e.xOld, e.yOld, e.zOld)
        ).subtract(e.position())

        return e.boundingBox.move(offset)
    }

    /* =========================
       getLookVec
       ========================= */

    fun getLookVec(delta: Float): Vec3 {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return Vec3.ZERO

        val pitch = player.getViewXRot(delta)
        val yaw = player.getViewYRot(delta)

        return Rotation(pitch, yaw).asLookVec()
    }

    /* =========================
       getLerpedBoxForBox
       ========================= */

    fun getLerpedBoxForBox(e: Entity, delta: Float): AABB {
        val x = Mth.lerp(delta.toDouble(), e.xOld, e.x)
        val y = Mth.lerp(delta.toDouble(), e.yOld, e.y)
        val z = Mth.lerp(delta.toDouble(), e.zOld, e.z)

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

    /* =========================
       VertexConsumerProvider
       ========================= */

    fun getVertexConsumerProvider(): MultiBufferSource.BufferSource {
        return (Minecraft.getInstance() as IMinecraftClient)
            .getBufferBuilderStorage()
            .bufferSource()
    }
}
