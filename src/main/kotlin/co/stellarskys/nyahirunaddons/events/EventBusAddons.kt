package co.stellarskys.nyahirunaddons.events

import co.stellarskys.nyahirunaddons.events.core.InteractionEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.EventBus
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.BlockItem

@Module
object EventBusAddons {
    init {
        UseBlockCallback.EVENT.register { player, _, hand, hitResult ->
            val stack = player.getItemInHand(hand)
            if (stack.item is BlockItem) {
                val eventPost = InteractionEvent.PlaceAttempt(player, stack, hitResult)
                EventBus.post(eventPost)
                return@register eventPost.result
            }
            val eventPost = InteractionEvent.ItemUseAttempt(player, stack)
            EventBus.post(eventPost)
            eventPost.result
        }

        UseItemCallback.EVENT.register { player, _, hand ->
            val stack = player.getItemInHand(hand)
            val eventPost = InteractionEvent.ItemUseAttempt(player, stack)
            EventBus.post(eventPost)
            eventPost.result
        }

        UseEntityCallback.EVENT.register { player, _, hand, entity, _ ->
            val stack = player.getItemInHand(hand)
            val event = InteractionEvent.EntityUseAttempt(player, stack, entity)
            EventBus.post(event)
            event.result
        }

        ClientPreAttackCallback.EVENT.register { _, player, _ ->
            val stack = player.getItemInHand(InteractionHand.MAIN_HAND)
            val eventPost = InteractionEvent.PreAttackAttempt(player, stack)
            EventBus.post(eventPost)
            eventPost.result
        }

        AttackBlockCallback.EVENT.register { player, _, hand, pos, _ ->
            val stack = player.getItemInHand(hand)
            val eventPost = InteractionEvent.BlockAttackAttempt(player, stack, pos)
            EventBus.post(eventPost)
            eventPost.result
        }

        AttackEntityCallback.EVENT.register { player, _, hand, entity, _ ->
            val stack = player.getItemInHand(hand)
            val event = InteractionEvent.EntityAttackAttempt(player, stack, entity)
            EventBus.post(event)
            event.result
        }
    }
}
