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
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.TrappedChestBlock
import net.minecraft.world.phys.AABB
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import java.awt.Color
import java.util.ArrayList

@Module
object mimicChest : Feature("renderHighlight", island = SkyBlockIsland.THE_CATACOMBS), RenderListener {

    val mimicChestConfig by config.property<Boolean>("renderHighlightMimicChest")
    val mimicChestColorFaceConfig by config.property<Color>("renderHighlightMimicChestColorFace")
    val mimicChestColorLineConfig by config.property<Color>("renderHighlightMimicChestColorLine")
    private val chestList: MutableList<BlockPos> = ArrayList()

    override fun initialize() {
        EventManager.eventManager.subscribe(RenderListener::class.java, this)

        on<TickEvent.Client> {
            if (!mimicChestConfig) {
                chestList.clear()
                return@on
            }
            chestList.clear()

            val client = Minecraft.getInstance()
            val level = client.level ?: return@on

            val center = client.player?.blockPosition()
            center?.let { centerPos  ->
                for (pos in BlockPos.betweenClosed(
                    centerPos.offset(-16, -8, -16),
                    centerPos.offset(16, 8, 16)
                )) {
                    if (level.getBlockState(pos).block is TrappedChestBlock) {
                        chestList.add(pos.immutable())
                    }
                }
            }
        }
    }

    override fun onRender(matrixStack: PoseStack, partialTicks: Float) {
        if (chestList.isEmpty()) return

        for (pos in chestList) {
            matrixStack.pushPose()
            val box = AABB(pos)

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
                    mimicChestColorFaceConfig,
                    false
                )
            }

            for (e in edges) {
                RenderUtils.drawSingleLine(
                    matrixStack,
                    v[e[0]],
                    v[e[1]],
                    mimicChestColorLineConfig,
                    false
                )
            }

            matrixStack.popPose()
        }
    }
}
