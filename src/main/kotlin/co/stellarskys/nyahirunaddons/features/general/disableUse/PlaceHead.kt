package co.stellarskys.nyahirunaddons.features.general.disableUse

import co.stellarskys.nyahirunaddons.events.core.InteractionEvent
import co.stellarskys.nyahirunaddons.features.general.disableUse.utils.*
import co.stellarskys.nyahirunaddons.features.DisableUse
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.Blocks.PLAYER_HEAD

@Module
object PlaceHead : Feature("DisableUse") {
    override fun initialize() {
        on<InteractionEvent.PlaceAttempt> { event ->
            if (!DisableUse.placeHead || !event.isPlacing(PLAYER_HEAD)) return@on

            event.cancelWith(InteractionResult.SUCCESS)
        }
    }
}
