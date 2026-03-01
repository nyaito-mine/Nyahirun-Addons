package co.skyblock.features.nyahirunaddons.na1.disableUse

import co.skyblock.events.core.InteractionEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.TickEvent
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

    private var relicTickTime = 0

    override fun initialize() {
        on<TickEvent.Client> {
            if (relicTickTime > 0) relicTickTime--
        }

        on<InteractionEvent.ItemUseAttempt> { event ->
            if (!sbMenuConfig) return@on
            val stack = event.stack
            val useItem = stack.item
            val isRelic = useItem.name.string.contains("Corrupted") && useItem.name.string.contains("Relic")

            if (useItem == NETHER_STAR || useItem == FILLED_MAP || useItem == FEATHER) {
                event.result = InteractionResult.FAIL
                event.cancel()
            }

            if (isRelic && relicTickTime > 0) {
                event.result = InteractionResult.FAIL
                event.cancel()
            }

            return@on
        }

        on<InteractionEvent.PlaceAttempt> { event ->
            if (!sbMenuConfig) return@on
            val stack = event.stack
            val useItem = stack.item
            val isRelic = useItem.name.string.contains("Corrupted") && useItem.name.string.contains("Relic")

            if (useItem == NETHER_STAR || useItem == FILLED_MAP || useItem == FEATHER) {
                event.result = InteractionResult.FAIL
                event.cancel()
            }

            if (isRelic && relicTickTime > 0) {
                event.result = InteractionResult.FAIL
                event.cancel()
            } else if (isRelic && relicTickTime == 0) {
                relicTickTime = 5
            }

            return@on
        }

        on<InteractionEvent.PreAttackAttempt> { event ->
            if (!sbMenuConfig) return@on
            val stack = event.stack
            val useItem = stack.item
            val isRelic = useItem.name.string.contains("Corrupted") && useItem.name.string.contains("Relic")

            if (useItem == NETHER_STAR || useItem == FILLED_MAP || useItem == FEATHER) {
                event.result = true
                event.cancel()
            }

            if (isRelic && relicTickTime > 0) {
                event.result = true
                event.cancel()
            }

            return@on
        }
    }
}
