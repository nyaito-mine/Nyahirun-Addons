package co.stellarskys.nyahirunaddons.features.general.nonCategory.commandKeys

import co.stellarskys.nyahirunaddons.config.CommandKeysConfig
import co.stellarskys.nyahirunaddons.config.CommandKeyEntry

object ConfigResponse {
    data class Entry(
        val id: String,
        val key: String,
        val command: String,
        val enabled: Boolean,
        val cooldownTicks: Int
    )

    fun getCommands(): List<Entry> {
        return CommandKeysConfig.getEntries().map { entry ->
            Entry(
                id = entry.id,
                key = entry.keyCode,
                command = entry.command,
                enabled = entry.enabled,
                cooldownTicks = entry.cooldownTicks
            )
        }
    }

    fun updateCommand(entry: Entry) {
        CommandKeysConfig.update { entries ->
            val index = entries.indexOfFirst { it.id == entry.id }
            if (index != -1) {
                entries[index] = CommandKeyEntry(
                    id = entry.id,
                    enabled = entry.enabled,
                    keyCode = entry.key,
                    command = entry.command,
                    cooldownTicks = entry.cooldownTicks
                )
            }
        }
    }

    fun addCommand(entry: Entry) {
        CommandKeysConfig.update { entries ->
            entries.add(
                CommandKeyEntry(
                    id = entry.id,
                    enabled = entry.enabled,
                    keyCode = entry.key,
                    command = entry.command,
                    cooldownTicks = entry.cooldownTicks
                )
            )
        }
    }

    fun deleteCommand(id: String) {
        CommandKeysConfig.update { entries ->
            entries.removeIf { it.id == id }
        }
    }
}

