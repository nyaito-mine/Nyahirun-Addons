package co.skyblock.features.nyahirunaddons.na1.disableUse

import co.skyblock.events.core.InteractionEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Blocks.HOPPER
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object placeTuba : Feature("disableUse", island = SkyBlockIsland.THE_CATACOMBS) {

    val placeTubaConfig by config.property<Boolean>("disableUsePlaceTuba")

    override fun initialize() {
        on<InteractionEvent.PlaceAttempt> { event ->
            if (!placeTubaConfig) return@on

            val stack = event.stack

            val useItem = stack.item as? BlockItem ?: return@on

            if (useItem.block == HOPPER) {
                event.result = InteractionResult.SUCCESS
                event.cancel()
            }

            return@on
        }
    }
}
