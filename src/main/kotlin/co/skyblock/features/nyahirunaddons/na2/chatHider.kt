package co.skyblock.features.nyahirunaddons.na2

import co.skyblock.utils.ListEntry
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.client.Minecraft
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object chatHider : Feature("chatHider", island = SkyBlockIsland.THE_CATACOMBS) {
    val obtainedConfig by config.property<Boolean>("chathiderObtained")
    val milestoneConfig by config.property<Boolean>("chathiderMilestone")
    val killComboConfig by config.property<Boolean>("chathiderKillCombo")
    val bossConfig by config.property<Boolean>("chathiderBoss")
    val npcMortConfig by config.property<Boolean>("chathiderNPCMort")
    val teleportCooldownConfig by config.property<Boolean>("chathiderTeleportCooldown")
    val implosionConfig by config.property<Boolean>("chathiderImplosion")
    val trapRoomConfig by config.property<Boolean>("chathiderTrapRoom")
    val leverConfig by config.property<Boolean>("chathiderLever")
    val chestConfig by config.property<Boolean>("chathiderChest")
    val icePathConfig by config.property<Boolean>("chathiderIcePath")
    val mysticalForceConfig by config.property<Boolean>("chathiderMysticalForce")
    val lostAdventureConfig by config.property<Boolean>("chathiderLostAdventure")
    val essenceConfig by config.property<Boolean>("chathiderEssence")
    val blessingConfig by config.property<Boolean>("chathiderBlessing")

    private val chatHiderConfigs = listOf(
        ListEntry({ obtainedConfig }, "has obtained"),
        ListEntry({ milestoneConfig }, "Milestone"),
        ListEntry({ killComboConfig }, "Kill Combo"),
        ListEntry({ bossConfig }, "[BOSS] "),
        ListEntry({ npcMortConfig }, "[NPC] Mort"),
        ListEntry({ teleportCooldownConfig }, "There are blocks in the way!"),
        ListEntry({ implosionConfig }, "Your Implosion hit"),
        ListEntry({ trapRoomConfig }, "You cannot use abilities in this room!"),
        ListEntry({ leverConfig }, "This lever has already been used.", "You hear the sound of something opening..."),
        ListEntry({ chestConfig }, "This chest has already been searched!", "That chest is locked!"),
        ListEntry({ icePathConfig }, "You cannot hit the silverfish while it's moving!", "You cannot move the silverfish in that direction!"),
        ListEntry({ mysticalForceConfig }, "A mystical force in this room prevents you"),
        ListEntry({ lostAdventureConfig }, "You hear the sound of something opening"),
        ListEntry({ essenceConfig }, "You found a Wither Essence! Everyone gains an extra essence!"),
        ListEntry({ blessingConfig }, "DUNGEON BUFF! You found", "Granted you ", "Also granted you ", "A Blessing of ", "DUNGEON BUFF! A Blessing of ")
    )

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            val client = Minecraft.getInstance()
            if (client.player == null) return@on

            for (entry in chatHiderConfigs) {
                val enabled = entry.enabled()
                val triggerList = listOf(
                    entry.stringFirst,
                    entry.stringSecond,
                    entry.stringThird,
                    entry.stringFourth,
                    entry.stringFifth
                )

                for (trigger in triggerList) {
                    if (enabled && trigger != "" && msg.contains(trigger)) {
                        event.cancel()
                        break
                    }
                }
            }

            return@on
        }
    }
}