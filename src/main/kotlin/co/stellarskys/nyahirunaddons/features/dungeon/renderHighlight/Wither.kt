package co.stellarskys.nyahirunaddons.features.dungeon.renderHighlight

import co.stellarskys.nyahirunaddons.api.render.world.Render3D
import co.stellarskys.nyahirunaddons.events.core.BossEvent
import co.stellarskys.nyahirunaddons.events.core.RenderEvent
import co.stellarskys.nyahirunaddons.features.RenderHighlight
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.api.zenith.world
import co.stellarskys.stella.events.core.LocationEvent
import net.minecraft.world.entity.boss.wither.WitherBoss
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object Wither : Feature("RenderHighlight", island = SkyBlockIsland.THE_CATACOMBS) {
    private val TargetList = mutableSetOf<WitherBoss>()
    private var isGoldor = false
    val depth = false

    override fun initialize() {
        on<TickEvent.Client> {
            if (!RenderHighlight.Wither) {
                TargetList.clear()
                isGoldor = false
                return@on
            }

            val entities = world?.entitiesForRendering() ?: return@on
            for (entity in entities) {
                if (entity is WitherBoss && entity.isAlive && entity.maxHealth != 300.0f) {
                    TargetList.add(entity)
                }
            }
            TargetList.removeIf { entity -> !entity.isAlive }
        }

        on<BossEvent.PhaseEvent> { event ->
            if (!RenderHighlight.WitherGoldorTracer) {
                isGoldor = false
                return@on
            }

            when (event.phase) {
                3 -> if (event.phaseEvent == "Goldor") isGoldor = true else if (event.phaseEvent == "End") isGoldor = false
            }
        }

        on<LocationEvent.ServerChange> {
            TargetList.clear()
            isGoldor = false
        }

        on<RenderEvent.Draw> { event ->
            if (!RenderHighlight.Wither) return@on
            for (entity in TargetList) {
                var box = Render3D.getLerpedBoxForBox(entity)
                if (RenderHighlight.WitherScale >= 1) box = box.inflate(RenderHighlight.WitherScale / 20.0)

                Render3D.drawBox(
                    event.context,
                    box,
                    RenderHighlight.WitherLineColor,
                    RenderHighlight.WitherFillColor,
                    depth
                )

                if (isGoldor) {
                    val cam = Render3D.getCameraPos()
                    val start = Render3D.getLookVec().multiply(1.0, 1.0, 1.0)
                    val end = box.center.subtract(cam)
                    Render3D.drawSingleLine(
                        event.context,
                        start,
                        end,
                        RenderHighlight.WitherGoldorTracerColor,
                        depth
                    )
                }
            }
        }
    }
}
