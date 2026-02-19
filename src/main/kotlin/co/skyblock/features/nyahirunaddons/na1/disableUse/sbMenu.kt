package co.skyblock.features.nyahirunaddons.na1.disableUse

import co.skyblock.events.core.InteractionEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Items.NETHER_STAR
import net.minecraft.world.item.Items.FILLED_MAP
import net.minecraft.world.item.Items.FEATHER
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object sbMenu : Feature("disableUse", island = SkyBlockIsland.THE_CATACOMBS) {

    val sbMenuConfig by config.property<Boolean>("disableUseSBMenu")

    override fun initialize() {
        on<InteractionEvent.ItemUseAttempt> { event ->
            if (!sbMenuConfig) return@on
            val stack = event.stack
            val useItem = stack.item

            if (useItem == NETHER_STAR || useItem == FILLED_MAP || useItem == FEATHER) {
                event.result = InteractionResult.FAIL
                event.cancel()
            }

            return@on
        }

        on<InteractionEvent.PlaceAttempt> { event ->
            if (!sbMenuConfig) return@on
            val stack = event.stack
            val useItem = stack.item

            if (useItem == NETHER_STAR || useItem == FILLED_MAP || useItem == FEATHER) {
                event.result = InteractionResult.FAIL
                event.cancel()
            }

            return@on
        }

        on<InteractionEvent.PreAttackAttempt> { event ->
            if (!sbMenuConfig) return@on
            val stack = event.stack
            val useItem = stack.item

            if (useItem == NETHER_STAR || useItem == FILLED_MAP || useItem == FEATHER) {
                event.result = true
                event.cancel()
            }

            return@on
        }
    }
}
