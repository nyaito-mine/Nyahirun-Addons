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
import net.minecraft.world.entity.item.ItemEntity
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import java.awt.Color
import java.util.ArrayList

@Module
object dropItem : Feature("renderHighlight", island = SkyBlockIsland.THE_CATACOMBS), RenderListener {

    val dropItemConfig by config.property<Boolean>("renderHighlightDropItem")
    val dropItemColorConfig by config.property<Color>("renderHighlightDropItemColor")
    private val itemList: MutableList<ItemEntity> = ArrayList()

    private val itemNames = listOf(
        "first draft",
        "decoy",
        "chest key",
        "fel pearl",
        "inflatable jerry",
        "spirit leap",
        "superboom tnt",
        "trap",
        "candycomb",
        "training",
        "healing",
        "revive",
        "secret dye",
        "defuse kit",
        "training weights",
        "treasure talisman"
    )


    override fun initialize() {
        EventManager.eventManager.subscribe(RenderListener::class.java, this)

        on<TickEvent.Client> {
            if (!dropItemConfig) {
                itemList.clear()
                return@on
            }
            itemList.clear()

            val client = Minecraft.getInstance()
            val player = client.player ?: return@on
            val level = client.level ?: return@on

            val maxDistSq = 16 * 16

            for (entity in level.entitiesForRendering()) {
                if (entity is ItemEntity) {
                    if (player.distanceToSqr(entity) > maxDistSq) continue

                    val stack = entity.item
                    if (stack.isEmpty) continue

                    val name = stack.hoverName.string.lowercase()

                    for (filter in itemNames) {
                        if (name.contains(filter)) {
                            itemList.add(entity)
                            break
                        }
                    }
                }
            }
        }
    }

    override fun onRender(matrixStack: PoseStack, partialTicks: Float) {
        if (itemList.isEmpty()) return

        for (item in itemList) {
            matrixStack.pushPose()
            val box = RenderUtils.getLerpedBoxForBox(item, partialTicks).inflate(0.1)

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

//            int[][] edges = {
//                    {0,1},{0,2},{1,3},{2,3},
//                    {4,5},{4,6},{5,7},{6,7},
//                    {0,4},{1,5},{2,6},{3,7}
//            };
            for (f in faces) {
                RenderUtils.drawQuadfill(
                    matrixStack,
                    v[f[0]],
                    v[f[1]],
                    v[f[2]],
                    v[f[3]],
                    dropItemColorConfig,
                    true
                )
            }

//            for (int[] e : edges) {
//                RenderUtils.drawSingleLine(
//                        matrixStack,
//                        v[e[0]],
//                        v[e[1]],
//                        1f, 0f, 0f, 1f,
//                        true
//                );
//            }
            matrixStack.popPose()
        }
    }
}
