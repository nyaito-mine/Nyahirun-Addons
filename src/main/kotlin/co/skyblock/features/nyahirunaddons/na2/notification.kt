package co.skyblock.features.nyahirunaddons.na2

import co.skyblock.utils.ClientDelay
import co.skyblock.utils.ListEntry
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object notification : Feature("notification", island = SkyBlockIsland.THE_CATACOMBS) {
    val enragedConfig by config.property<Boolean>("notificationEnraged")
    val gateConfig by config.property<Boolean>("notificationGate")
    val coreLeapConfig by config.property<Boolean>("notificationCoreLeap")
    val necronLeapConfig by config.property<Boolean>("notificationNecronLeap")
    val ragnarockConfig by config.property<Boolean>("notificationRagnarock")
    val chestLogConfig by config.property<Boolean>("notificationChestLog")
    val maskConfig by config.property<Boolean>("notificationMask")
    val keyPickConfig by config.property<Boolean>("notificationKeyPick")

    private val notificationConfigs = listOf(
        ListEntry({ enragedConfig }, "⚠ Maxor is enraged! ⚠", "§l§cWish"),
        ListEntry({ gateConfig }, "The gate has been destroyed!", "§l§cGate Breaked"),
        ListEntry({ coreLeapConfig }, "The Core entrance is opening!", "§l§cLeap"),
        ListEntry({ necronLeapConfig }, "Goodbye.", "§l§cLeap"),
        ListEntry({ ragnarockConfig }, "I no longer wish to fight, but I know that will not stop you.", "§l§cRagnarock"),
        ListEntry({ chestLogConfig }, "That chest is locked!", "§l§cLocked"),
        ListEntry({ maskConfig }, "Bonzo's Mask saved your life!", "§l§bBonzo's Mask"),
        ListEntry({ maskConfig }, "Second Wind Activated! Your Spirit Mask saved your life!", "§l§bSpirit Mask"),
        ListEntry({ maskConfig }, "Your Phoenix Pet saved you from certain death!", "§l§bPhoenix"),
        ListEntry({ keyPickConfig }, "has obtained Wither Key!", "§l§eWither Key Pick"),
        ListEntry({ keyPickConfig }, "has obtained Blood Key!", "§l§eBlood Key Pick")
    )

    override fun initialize() {
        on<TickEvent.Client> {
            ClientDelay.tick()
        }

        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            val client = Minecraft.getInstance()
            if (client.player == null) return@on

            for (entry in notificationConfigs) {
                val enabled = entry.enabled()
                val trigger = entry.stringFirst
                val title = entry.stringSecond

                if (enabled && msg.contains(trigger)) {
                    ClientDelay.runLater(2, Runnable {
                        client.gui.setTitle(Component.literal(title))
                        client.gui.setTimes(0, 30, 0)
                        client.player?.playSound(
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            1.0f,
                            0.5f
                        )
                    })
                }
            }

            return@on
        }
    }
}