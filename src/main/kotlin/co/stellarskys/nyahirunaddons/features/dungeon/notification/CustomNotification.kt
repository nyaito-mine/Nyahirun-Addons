package co.stellarskys.nyahirunaddons.features.dungeon.notification

import co.stellarskys.nyahirunaddons.config.gui.pages.Notification
import co.stellarskys.nyahirunaddons.features.dungeon.notification.Notification as Noti
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.zenith.player
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.features.Feature
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier

@Module
object CustomNotification : Feature("Notification") {
    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            for (entry in Notification().notificationConfig) {
                if (entry.trigger.isEmpty()) continue
                if (event.stripped.contains(entry.trigger) && entry.enabled) {
                    val string = entry.notification.replace("^", "§")
                    Noti.renderTitle = string
                    Noti.renderTicks = entry.displayTicks
                    val id = Identifier.tryParse(entry.sound) ?: Identifier.parse("entity.experience_orb.pickup")
                    val soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(id)
                    soundEvent?.let { player?.playSound(it, entry.soundVolume, entry.soundPitch) }
                }
            }
        }
    }
}