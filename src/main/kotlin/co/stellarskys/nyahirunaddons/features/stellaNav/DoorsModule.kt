package co.stellarskys.nyahirunaddons.features.stellaNav

import co.stellarskys.nyahirunaddons.api.render.world.Render3D
import co.stellarskys.nyahirunaddons.events.core.RenderEvent
import co.stellarskys.nyahirunaddons.features.StellaNavExtra
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.dungeons.Dungeon
import co.stellarskys.stella.api.dungeons.utils.DoorState
import co.stellarskys.stella.api.dungeons.utils.DoorType
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.features.stellanav.BoxWitherDoors
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object DoorsModule : Feature("boxWitherDoors", island = SkyBlockIsland.THE_CATACOMBS) {
    override fun initialize() {
        on<RenderEvent.Draw> { event ->
            if (!StellaNavExtra.DoorsFill && !StellaNavExtra.DoorsTracer) return@on
            if (BoxWitherDoors.bloodOpen) return@on

            val color = if (BoxWitherDoors.keyObtained) StellaNavExtra.DoorsFillKeyColor else StellaNavExtra.DoorsFillNoKeyColor
            val camera = Render3D.getCameraPos()
            val start = Render3D.getLookVec().multiply(1.0, 1.0, 1.0)

            Dungeon.doors.forEach { door ->
                if (door == null || door.opened) return@forEach
                if (door.state != DoorState.DISCOVERED) return@forEach
                if (door.type !in setOf(DoorType.WITHER, DoorType.BLOOD)) return@forEach

                val (x, y, z) = door.getPos()
                val pos = Vec3(x.toDouble(), y.toDouble(), z.toDouble())
                val hw = 3.0 / 2.0
                val box = AABB(pos.x + 0.5 - hw, pos.y, pos.z + 0.5 - hw, pos.x + 0.5 + hw, pos.y + 4.0, pos.z + 0.5 + hw)

                Render3D.drawBox(
                    ctx = event.context,
                    box = box,
                    fillColor = color,
                    depthtest = true,
                )

                if (StellaNavExtra.DoorsTracer) {
                    val color = if (BoxWitherDoors.keyObtained) StellaNavExtra.DoorsTracerKeyColor else StellaNavExtra.DoorsTracerNoKeyColor
                    Render3D.drawSingleLine(
                        ctx = event.context,
                        start = start,
                        end = box.center.subtract(camera),
                        color = color,
                        depthtest = true,
                    )
                }
            }
        }
    }
}