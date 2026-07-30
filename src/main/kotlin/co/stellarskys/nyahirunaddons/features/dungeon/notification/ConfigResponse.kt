package co.stellarskys.nyahirunaddons.features.dungeon.notification

import co.stellarskys.nyahirunaddons.config.NotificationConfig
import co.stellarskys.nyahirunaddons.config.NotificationEntry
import java.util.UUID

object ConfigResponse {
    data class Entry(
        val id: String,
        val enabled: Boolean,
        val notification: String,
        val trigger: String,
        val displayTicks: Int,
        val sound: String,
        val soundVolume: Float,
        val soundPitch: Float
    )

    fun getCommands(): List<Entry> {
        return NotificationConfig.getEntries().map { entry ->
            Entry(
                id = entry.id,
                enabled = entry.enabled,
                notification = entry.notification,
                trigger = entry.trigger,
                displayTicks = entry.displayTicks,
                sound = entry.sound,
                soundVolume = entry.soundVolume,
                soundPitch = entry.soundPitch
            )
        }
    }

    fun updateCommand(entry: Entry) {
        NotificationConfig.update { entries ->
            val index = entries.indexOfFirst { it.id == entry.id }
            if (index != -1) {
                entries[index] = NotificationEntry(
                    id = entry.id,
                    enabled = entry.enabled,
                    notification = entry.notification,
                    trigger = entry.trigger,
                    displayTicks = entry.displayTicks,
                    sound = entry.sound,
                    soundVolume = entry.soundVolume,
                    soundPitch = entry.soundPitch
                )
            }
        }
    }

    fun addCommand(entry: Entry) {
        NotificationConfig.update { entries ->
            entries.add(
                NotificationEntry(
                    id = entry.id,
                    enabled = entry.enabled,
                    notification = entry.notification,
                    trigger = entry.trigger,
                    displayTicks = entry.displayTicks,
                    sound = entry.sound,
                    soundVolume = entry.soundVolume,
                    soundPitch = entry.soundPitch
                )
            )
        }
    }

    fun deleteCommand(id: String) {
        NotificationConfig.update { entries ->
            entries.removeIf { it.id == id }
        }
    }
}

