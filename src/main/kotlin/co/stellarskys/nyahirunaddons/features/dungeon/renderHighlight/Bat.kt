package co.stellarskys.nyahirunaddons.features.dungeon.renderHighlight

import co.stellarskys.nyahirunaddons.api.render.world.Render3D
import co.stellarskys.nyahirunaddons.events.core.RenderEvent
import co.stellarskys.nyahirunaddons.features.RenderHighlight
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.zenith.world
import co.stellarskys.stella.events.core.LocationEvent
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import net.minecraft.world.entity.ambient.Bat
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object Bat : Feature("RenderHighlight", island = SkyBlockIsland.THE_CATACOMBS) {
    private val TargetList = mutableSetOf<Bat>()

    override fun initialize() {
        on<TickEvent.Client> {
            if (!RenderHighlight.Bat) {
                TargetList.clear()
                return@on
            }

            val entities = world?.entitiesForRendering() ?: return@on
            for (entity in entities) {
                if (entity is Bat && entity.isAlive && entity.maxHealth == 100.0f) {
                    TargetList.add(entity)
                }
            }
            TargetList.removeIf { entity -> !entity.isAlive }
        }

        on<LocationEvent.ServerChange> {
            TargetList.clear()
        }

        on<RenderEvent.Draw> { event ->
            if (!RenderHighlight.Bat) return@on
            for (entity in TargetList) {
                var box = Render3D.getLerpedBoxForBox(entity)
                if (RenderHighlight.BatScale >= 1) box = box.inflate(RenderHighlight.BatScale / 20.0)

                Render3D.drawBox(
                    event.context,
                    box,
                    RenderHighlight.BatLineColor,
                    RenderHighlight.BatFillColor,
                    false
                )

                if (RenderHighlight.BatTracer) {
                    val cam = Render3D.getCameraPos()
                    val start = Render3D.getLookVec().multiply(1.0, 1.0, 1.0)
                    val end = box.center.subtract(cam)
                    Render3D.drawSingleLine(
                        event.context,
                        start,
                        end,
                        RenderHighlight.BatTracerColor,
                        false
                    )
                }
            }
        }
    }
}