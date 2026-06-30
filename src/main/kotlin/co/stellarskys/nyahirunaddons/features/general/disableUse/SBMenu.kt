package co.stellarskys.nyahirunaddons.features.general.disableUse

import co.stellarskys.nyahirunaddons.events.core.InteractionEvent
import co.stellarskys.nyahirunaddons.features.general.disableUse.utils.*
import co.stellarskys.nyahirunaddons.features.DisableUse
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.handlers.Signal.fakeMessage
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks

@Module
object SBMenu : Feature("DisableUse") {
    private val blockedItems = setOf(Items.NETHER_STAR, Items.FILLED_MAP, Items.FEATHER)
    private var relicTickTime = 0

    private fun isBlockedItem(stack: ItemStack): Boolean =
        stack.item in blockedItems

    private fun isCorruptedRelic(stack: ItemStack): Boolean {
        val itemName = stack.hoverName.string.lowercase()
        fakeMessage("Checking item: $itemName")
        return itemName.contains("corrupted") && itemName.contains("relic")
    }

    private fun isBlockTime(): Boolean =
        relicTickTime > 0

    override fun initialize() {
        on<TickEvent.Client> {
            if (relicTickTime > 0) {
                relicTickTime--
                fakeMessage("Corrupted Relic cooldown: $relicTickTime")
            }
        }

        on<InteractionEvent.PlaceAttempt> { event ->
            if (!DisableUse.sbMenu) return@on

            if (isCorruptedRelic(event.stack) && (event.isUsingAtBlock(Blocks.CAULDRON) || event.isUsingAtBlock(Blocks.ANVIL))) {
                relicTickTime = 5
                return@on
            }

            if (isBlockedItem(event.stack) || isBlockTime()) {
                event.cancelWith(InteractionResult.FAIL)
            }
        }

        on<InteractionEvent.EntityUseAttempt> { event ->
            if (!DisableUse.sbMenu) return@on

            if (isCorruptedRelic(event.stack) && event.entity is ArmorStand) {
                relicTickTime = 5
                return@on
            }
        }

        on<InteractionEvent.PreAttackAttempt> { event ->
            if (!DisableUse.sbMenu) return@on

            if (isBlockedItem(event.stack) || isBlockTime()) {
                event.cancelWith(true)
            }
        }
    }

    override fun onUnregister() {
        if (relicTickTime > 0) relicTickTime = 0
    }
}
