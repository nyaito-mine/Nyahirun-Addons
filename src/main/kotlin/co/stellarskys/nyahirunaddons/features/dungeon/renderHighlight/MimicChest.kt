package co.stellarskys.nyahirunaddons.features.dungeon.renderHighlight

import co.stellarskys.nyahirunaddons.api.render.world.Render3D
import co.stellarskys.nyahirunaddons.events.core.RenderEvent
import co.stellarskys.nyahirunaddons.features.RenderHighlight
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.api.zenith.client
import co.stellarskys.stella.api.zenith.world
import co.stellarskys.stella.events.core.LocationEvent
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.TrappedChestBlock
import net.minecraft.world.phys.AABB
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object MimicChest : Feature("RenderHighlight", island = SkyBlockIsland.THE_CATACOMBS) {
    private val TargetList = mutableSetOf<BlockPos>()

    override fun initialize() {
        on<TickEvent.Client> {
            if (!RenderHighlight.MimicChest) {
                TargetList.clear()
                return@on
            }

            val center = client.player?.blockPosition()
            center?.let { centerPos  ->
                for (pos in BlockPos.betweenClosed(
                    centerPos.offset(-16, -8, -16),
                    centerPos.offset(16, 8, 16)
                )) {
                    if (world?.getBlockState(pos)?.block is TrappedChestBlock) {
                        TargetList.add(pos.immutable())
                    }
                }
            }
            TargetList.removeIf { pos -> world?.getBlockState(pos)?.block !is TrappedChestBlock }
        }

        on<LocationEvent.ServerChange> {
            TargetList.clear()
        }

        on<RenderEvent.Draw> { event ->
            if (!RenderHighlight.MimicChest) return@on
            for (pos in TargetList) {
                val box = AABB(pos)

                Render3D.drawBox(
                    event.context,
                    box,
                    RenderHighlight.MimicChestLineColor,
                    RenderHighlight.MimicChestFillColor,
                    false,
                )
            }
        }
    }
}
