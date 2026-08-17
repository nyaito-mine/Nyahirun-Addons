package co.stellarskys.nyahirunaddons.features.general

import co.stellarskys.nyahirunaddons.features.ChatHider
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.features.Feature
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object ChatHider : Feature("ChatHider") {
    private val chatHiderConfigs = listOf(
        Pair(ChatHider.Obtained, listOf("has obtained")),
        Pair(ChatHider.Milestone, listOf("Milestone")),
        Pair(ChatHider.KillCombo, listOf("Kill Combo")),
        Pair(ChatHider.Boss, listOf("[BOSS] ")),
        Pair(ChatHider.NPCMort, listOf("[NPC] Mort")),
        Pair(ChatHider.TeleportCooldown, listOf("There are blocks in the way!")),
        Pair(ChatHider.Implosion, listOf("Your Implosion hit")),
        Pair(ChatHider.TrapRoom, listOf("You cannot use abilities in this room!")),
        Pair(ChatHider.Lever, listOf("This lever has already been used.", "You hear the sound of something opening...")),
        Pair(ChatHider.Chest, listOf("This chest has already been searched!", "That chest is locked!")),
        Pair(ChatHider.IcePath, listOf("You cannot hit the silverfish while it's moving!", "You cannot move the silverfish in that direction!")),
        Pair(ChatHider.MysticalForce, listOf("A mystical force in this room prevents you")),
        Pair(ChatHider.LostAdventure, listOf("You hear the sound of something opening")),
        Pair(ChatHider.Essence, listOf("You found a Wither Essence! Everyone gains an extra essence!")),
        Pair(ChatHider.Blessing, listOf("DUNGEON BUFF! You found", "Granted you ", "Also granted you ", "A Blessing of ", "DUNGEON BUFF! A Blessing of ")
        )
    )

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped
            if (chatHiderConfigs.any { entry ->
                    entry.first && entry.second.any(msg::contains)
                }
            ) {
                event.cancel()
            }
        }
    }
}
