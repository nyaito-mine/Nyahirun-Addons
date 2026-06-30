package co.stellarskys.nyahirunaddons.features.general.disableUse.utils

import co.stellarskys.nyahirunaddons.events.core.InteractionEvent
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block

internal fun InteractionEvent.PlaceAttempt.cancelWith(result: InteractionResult) {
    this.result = result
    cancel()
}

internal fun InteractionEvent.PreAttackAttempt.cancelWith(result: Boolean) {
    this.result = result
    cancel()
}

internal fun InteractionEvent.PlaceAttempt.usedBlock(): Block? =
    (stack.item as? BlockItem)?.block

internal fun InteractionEvent.PlaceAttempt.isPlacing(block: Block): Boolean =
    usedBlock() == block

internal fun InteractionEvent.PlaceAttempt.isPlacingOnSameBlock(block: Block): Boolean {
    if (usedBlock() != block) return false

    val clickedBlock = player.level().getBlockState(hitResult.blockPos).block
    return clickedBlock == block
}

internal fun InteractionEvent.PlaceAttempt.isUsingAtBlock(block: Block): Boolean {
    val clickedBlock = player.level().getBlockState(hitResult.blockPos).block
    return clickedBlock == block
}
