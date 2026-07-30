package co.stellarskys.nyahirunaddons.features.dungeon.renderHighlight

import co.stellarskys.nyahirunaddons.api.render.world.Render3D
import co.stellarskys.nyahirunaddons.events.core.RenderEvent
import co.stellarskys.nyahirunaddons.features.RenderHighlight
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.api.zenith.player
import co.stellarskys.stella.api.zenith.world
import co.stellarskys.stella.events.core.LocationEvent
import net.minecraft.world.entity.item.ItemEntity
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object SecretItem : Feature("RenderHighlight", island = SkyBlockIsland.THE_CATACOMBS) {
    private val TargetList = mutableSetOf<ItemEntity>()

    private val ItemNames = listOf(
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
        on<TickEvent.Client> {
            if (!RenderHighlight.SecretItem) {
                TargetList.clear()
                return@on
            }

            val player = player ?: return@on
            val entities = world?.entitiesForRendering() ?: return@on

            val maxDistSq = 16 * 16

            for (entity in entities) {
                if (entity !is ItemEntity || player.distanceToSqr(entity) > maxDistSq) continue

                val stack = entity.item
                if (stack.isEmpty) continue

                val name = stack.hoverName.string.lowercase()
                if (ItemNames.any(name::contains)) {
                    TargetList.add(entity)
                }
            }
            TargetList.removeIf { entity -> !entity.isAlive }
        }

        on<LocationEvent.ServerChange> {
            TargetList.clear()
        }

        on<RenderEvent.Draw> { event ->
            if (!RenderHighlight.SecretItem) return@on
            for (entity in TargetList) {
                var box = Render3D.getLerpedBoxForBox(entity)
                if (RenderHighlight.SecretItemScale >= 1) box = box.inflate(RenderHighlight.SecretItemScale / 20.0)

                Render3D.drawBox(
                    event.context,
                    box,
                    null,
                    RenderHighlight.SecretItemColor,
                    depthtest = true
                )
            }
        }
    }
}
