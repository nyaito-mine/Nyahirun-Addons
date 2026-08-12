package co.stellarskys.nyahirunaddons.config.gui.response

import co.stellarskys.nyahirunaddons.config.ConfigResponseEntry
import co.stellarskys.nyahirunaddons.config.ListConfigResponse
import co.stellarskys.nyahirunaddons.config.NotificationConfig
import co.stellarskys.nyahirunaddons.config.NotificationEntry

object NotificationResponse : ListConfigResponse<NotificationEntry, NotificationResponse.Entry>(
    getEntries = NotificationConfig::getEntries,
    updateEntries = NotificationConfig::update,
    configIdOf = { it.id },
    toResponseEntry = {
        Entry(
            id = it.id,
            enabled = it.enabled,
            notification = it.notification,
            trigger = it.trigger,
            displayTicks = it.displayTicks,
            sound = it.sound,
            soundVolume = it.soundVolume,
            soundPitch = it.soundPitch
        )
    },
    toConfigEntry = {
        NotificationEntry(
            id = it.id,
            enabled = it.enabled,
            notification = it.notification,
            trigger = it.trigger,
            displayTicks = it.displayTicks,
            sound = it.sound,
            soundVolume = it.soundVolume,
            soundPitch = it.soundPitch
        )
    }
) {
    data class Entry(
        override val id: String,
        val enabled: Boolean,
        val notification: String,
        val trigger: String,
        val displayTicks: Int,
        val sound: String,
        val soundVolume: Float,
        val soundPitch: Float
    ) : ConfigResponseEntry
}