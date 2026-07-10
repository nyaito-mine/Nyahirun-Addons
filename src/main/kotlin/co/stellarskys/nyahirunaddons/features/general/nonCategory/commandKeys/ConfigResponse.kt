package co.stellarskys.nyahirunaddons.features.general.nonCategory.commandKeys

import co.stellarskys.nyahirunaddons.config.CommandKeysConfig
import co.stellarskys.nyahirunaddons.config.CommandKeyEntry

object ConfigResponse {
    data class Command(
        val id: String,
        val key: String,
        val command: String,
        val enabled: Boolean,
        val cooldownTicks: Int
    )

    fun getCommands(): List<Command> {
        return CommandKeysConfig.getEntries().map { entry ->
            Command(
                id = entry.id,
                key = entry.keyCode,
                command = entry.command,
                enabled = entry.enabled,
                cooldownTicks = entry.cooldownTicks
            )
        }
    }

    fun updateCommand(command: Command) {
        CommandKeysConfig.update { entries ->
            val index = entries.indexOfFirst { it.id == command.id }
            if (index != -1) {
                entries[index] = CommandKeyEntry(
                    id = command.id,
                    enabled = command.enabled,
                    keyCode = command.key,
                    command = command.command,
                    cooldownTicks = command.cooldownTicks
                )
            }
        }
    }

    fun addCommand(command: Command) {
        CommandKeysConfig.update { entries ->
            entries.add(
                CommandKeyEntry(
                    id = command.id,
                    enabled = command.enabled,
                    keyCode = command.key,
                    command = command.command,
                    cooldownTicks = command.cooldownTicks
                )
            )
        }
    }

    fun deleteCommand(commandId: String) {
        CommandKeysConfig.update { entries ->
            entries.removeIf { it.id == commandId }
        }
    }
}

