package co.skyblock.features.nyahirunaddons.na1.disableUse

import co.skyblock.events.core.InteractionEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Blocks.SOUL_SAND
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object secondSoulSand : Feature("disableUse", island = SkyBlockIsland.THE_CATACOMBS) {

    val secondSoulSandConfig by config.property<Boolean>("disableUseSecondSoulSand")

    override fun initialize() {
        on<InteractionEvent.PlaceAttempt> { event ->
            if (!secondSoulSandConfig) return@on

            val player = event.player
            val stack = event.stack
            val hitResult = event.hitResult

            val useItem = stack.item as? BlockItem ?: return@on

            val level = player.level()
            val clickedBlock = level.getBlockState(hitResult.blockPos).block

            if (useItem.block == SOUL_SAND && clickedBlock == SOUL_SAND) {
                event.result = InteractionResult.FAIL
                event.cancel()
            }

            return@on
        }
    }
}
