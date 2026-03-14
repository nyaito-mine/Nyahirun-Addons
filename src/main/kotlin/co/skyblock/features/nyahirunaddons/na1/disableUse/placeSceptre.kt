package co.skyblock.features.nyahirunaddons.na1.disableUse

import co.skyblock.events.core.InteractionEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Blocks.ALLIUM
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object placeSceptre : Feature("disableUse", island = SkyBlockIsland.THE_CATACOMBS) {

    val placeBOLConfig by config.property<Boolean>("disableUsePlaceSceptre")

    override fun initialize() {
        on<InteractionEvent.PlaceAttempt> { event ->
            if (!placeBOLConfig) return@on

            val stack = event.stack

            val useItem = stack.item as? BlockItem ?: return@on

            if (useItem.block == ALLIUM) {
                event.result = InteractionResult.SUCCESS
                event.cancel()
            }

            return@on
        }
    }
}
