package co.stellarskys.nyahirunaddons.events

import co.stellarskys.nyahirunaddons.api.render.world.RenderContext
import co.stellarskys.nyahirunaddons.events.core.BossEvent
import co.stellarskys.nyahirunaddons.events.core.InteractionEvent
import co.stellarskys.nyahirunaddons.events.core.RenderEvent
import co.stellarskys.nyahirunaddons.utils.EventUtils.ReadOnly
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.dungeons.Dungeon.floor
import co.stellarskys.stella.events.EventBus
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.events.core.DungeonEvent
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.BlockItem
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

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

        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register { context ->
            EventBus.post(RenderEvent.Draw(RenderContext.fromContext(context)))
        }


        EventBus.on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped
            if (msg.contains(ReadOnly.dungeonStartTrigger)) floor?.let { EventBus.post(DungeonEvent.Start(it)) }
            if (msg.contains(ReadOnly.P1StartTrigger)) EventBus.post(BossEvent.PhaseEvent(1, "Start"))
            if (msg.contains(ReadOnly.P1EnragedTrigger)) EventBus.post(BossEvent.PhaseEvent(1, "Enraged"))
            if (msg.contains(ReadOnly.P1EndTrigger)) EventBus.post(BossEvent.PhaseEvent(1, "End"))
            if (msg.contains(ReadOnly.P2StartTrigger)) EventBus.post(BossEvent.PhaseEvent(2, "Start"))
            if (msg.contains(ReadOnly.P2ThunderTrigger)) EventBus.post(BossEvent.PhaseEvent(2, "Thunder"))
            if (msg.contains(ReadOnly.P2EnragedTrigger)) EventBus.post(BossEvent.PhaseEvent(2, "Enraged"))
            if (msg.contains(ReadOnly.P2EndTrigger)) EventBus.post(BossEvent.PhaseEvent(2, "End"))
            if (msg.contains(ReadOnly.P3StartTrigger)) EventBus.post(BossEvent.PhaseEvent(3, "Start"))
            if (msg.contains(ReadOnly.P3GoldorTrigger)) EventBus.post(BossEvent.PhaseEvent(3, "Goldor"))
            if (msg.contains(ReadOnly.P3EndTrigger)) EventBus.post(BossEvent.PhaseEvent(3, "End"))
            if (msg.contains(ReadOnly.P4StartTrigger)) EventBus.post(BossEvent.PhaseEvent(4, "Start"))
            if (msg.contains(ReadOnly.P4DropTrigger)) EventBus.post(BossEvent.PhaseEvent(4, "Drop"))
            if (msg.contains(ReadOnly.P4EndTrigger)) EventBus.post(BossEvent.PhaseEvent(4, "End"))
            if (msg.contains(ReadOnly.P5RagnarockTrigger)) EventBus.post(BossEvent.PhaseEvent(5, "Ragnarock"))
        }
    }
}
