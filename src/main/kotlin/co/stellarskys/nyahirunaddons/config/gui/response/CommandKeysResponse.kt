package co.stellarskys.nyahirunaddons.config.gui.response

import co.stellarskys.nyahirunaddons.config.CommandKeyEntry
import co.stellarskys.nyahirunaddons.config.CommandKeysConfig
import co.stellarskys.nyahirunaddons.config.ConfigResponseEntry
import co.stellarskys.nyahirunaddons.config.ListConfigResponse

object CommandKeysResponse : ListConfigResponse<CommandKeyEntry, CommandKeysResponse.Entry>(
    getEntries = CommandKeysConfig::getEntries,
    updateEntries = CommandKeysConfig::update,
    configIdOf = { it.id },
    toResponseEntry = {
        Entry(
            id = it.id,
            key = it.keyCode,
            command = it.command,
            enabled = it.enabled,
            cooldownTicks = it.cooldownTicks
        )
    },
    toConfigEntry = {
        CommandKeyEntry(
            id = it.id,
            enabled = it.enabled,
            keyCode = it.key,
            command = it.command,
            cooldownTicks = it.cooldownTicks
        )
    }
) {
    data class Entry(
        override val id: String,
        val key: String,
        val command: String,
        val enabled: Boolean,
        val cooldownTicks: Int
    ) : ConfigResponseEntry
}