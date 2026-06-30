package co.stellarskys.nyahirunaddons.features.general

import co.stellarskys.nyahirunaddons.utils.ListEntry
import co.stellarskys.nyahirunaddons.features.ChatHider
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.features.Feature
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object ChatHider : Feature("ChatHider") {
    private val chatHiderConfigs = listOf(
        ListEntry({ ChatHider.obtained }, "has obtained"),
        ListEntry({ ChatHider.milestone }, "Milestone"),
        ListEntry({ ChatHider.killCombo }, "Kill Combo"),
        ListEntry({ ChatHider.boss }, "[BOSS] "),
        ListEntry({ ChatHider.npcMort }, "[NPC] Mort"),
        ListEntry({ ChatHider.teleportCooldown }, "There are blocks in the way!"),
        ListEntry({ ChatHider.implosion }, "Your Implosion hit"),
        ListEntry({ ChatHider.trapRoom }, "You cannot use abilities in this room!"),
        ListEntry({ ChatHider.lever }, "This lever has already been used.", "You hear the sound of something opening..."),
        ListEntry({ ChatHider.chest }, "This chest has already been searched!", "That chest is locked!"),
        ListEntry(
            { ChatHider.icePath },
            "You cannot hit the silverfish while it's moving!",
            "You cannot move the silverfish in that direction!"
        ),
        ListEntry({ ChatHider.mysticalForce }, "A mystical force in this room prevents you"),
        ListEntry({ ChatHider.lostAdventure }, "You hear the sound of something opening"),
        ListEntry({ ChatHider.essence }, "You found a Wither Essence! Everyone gains an extra essence!"),
        ListEntry(
            { ChatHider.blessing },
            "DUNGEON BUFF! You found",
            "Granted you ",
            "Also granted you ",
            "A Blessing of ",
            "DUNGEON BUFF! A Blessing of "
        )
    )

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped
            if (chatHiderConfigs.any { entry ->
                    entry.enabled() && entry.triggers.any(msg::contains)
                }
            ) {
                event.cancel()
            }
        }
    }
}
