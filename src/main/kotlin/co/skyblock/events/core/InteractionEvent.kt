package co.skyblock.events.core

import co.stellarskys.stella.events.api.Event
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult

sealed class InteractionEvent {

    class PlaceAttempt(
        val player: Player,
        val stack: ItemStack,
        val hitResult: BlockHitResult
    ) : Event(cancelable = true) {
        var result: InteractionResult = InteractionResult.PASS
    }

    class ItemUseAttempt(
        val player: Player,
        val stack: ItemStack
    ) : Event(cancelable = true) {
        var result: InteractionResult = InteractionResult.PASS
    }

    class PreAttackAttempt(
        val player: Player,
        val stack: ItemStack,
    ) : Event(cancelable = true) {
        var result = false
    }

    class BlockAttackAttempt(
        val player: Player,
        val stack: ItemStack,
        val pos: BlockPos
    ) : Event(cancelable = true) {
        var result: InteractionResult = InteractionResult.PASS
    }

    class EntityAttackAttempt(
        val player: Player,
        val stack: ItemStack,
        val entity: Entity
    ) : Event(cancelable = true) {
        var result: InteractionResult = InteractionResult.PASS
    }
}
