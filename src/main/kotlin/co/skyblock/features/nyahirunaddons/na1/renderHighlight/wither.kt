package co.skyblock.features.nyahirunaddons.na1.renderHighlight

import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import co.skyblock.utils.render.listeners.RenderListener
import co.skyblock.utils.render.EventManager
import co.skyblock.utils.render.RenderUtils
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.boss.wither.WitherBoss
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import java.awt.Color
import java.util.ArrayList

@Module
object wither : Feature("renderHighlight", island = SkyBlockIsland.THE_CATACOMBS), RenderListener {

    val witherConfig by config.property<Boolean>("renderHighlightWither")
    val witherColorFaceConfig by config.property<Color>("renderHighlightWitherColorFace")
    val witherColorLineConfig by config.property<Color>("renderHighlightWitherColorLine")
    private val witherList: MutableList<WitherBoss> = ArrayList()

    override fun initialize() {
        EventManager.eventManager.subscribe(RenderListener::class.java, this)

        on<TickEvent.Client> {
            if (!witherConfig) {
                witherList.clear()
                return@on
            }
            witherList.clear()

            val client = Minecraft.getInstance()
            val level = client.level ?: return@on

            for (entity in level.entitiesForRendering()) {
                if (entity is WitherBoss) {
                    val isWitherborn = entity.maxHealth
                    if (isWitherborn == 300.0f) continue
                    witherList.add(entity)
                }
            }
        }
    }

    override fun onRender(matrixStack: PoseStack, partialTicks: Float) {
        if (witherList.isEmpty()) return

        for (wither in witherList) {
            matrixStack.pushPose()
            val box = RenderUtils.getLerpedBoxForBox(wither, partialTicks)

            val cam = RenderUtils.getCameraPos()
            val v = RenderUtils.getBoxVertices(box, cam)

            val faces = arrayOf(
                intArrayOf(0, 2, 3, 1),
                intArrayOf(4, 5, 7, 6),
                intArrayOf(0, 1, 5, 4),
                intArrayOf(2, 6, 7, 3),
                intArrayOf(0, 4, 6, 2),
                intArrayOf(1, 3, 7, 5)
            )

            val edges = arrayOf(
                intArrayOf(0, 1), intArrayOf(0, 2), intArrayOf(1, 3), intArrayOf(2, 3),
                intArrayOf(4, 5), intArrayOf(4, 6), intArrayOf(5, 7), intArrayOf(6, 7),
                intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7),
            )

            for (f in faces) {
                RenderUtils.drawQuadfill(
                    matrixStack,
                    v[f[0]],
                    v[f[1]],
                    v[f[2]],
                    v[f[3]],
                    witherColorFaceConfig,
                    false
                )
            }

            for (e in edges) {
                RenderUtils.drawSingleLine(
                    matrixStack,
                    v[e[0]],
                    v[e[1]],
                    witherColorLineConfig,
                    false
                )
            }

            matrixStack.popPose()
        }
    }
}
