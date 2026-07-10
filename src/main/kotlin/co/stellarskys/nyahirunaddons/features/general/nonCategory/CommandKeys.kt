package co.stellarskys.nyahirunaddons.features.general.nonCategory

import co.stellarskys.nyahirunaddons.api.GuiUtils
import co.stellarskys.nyahirunaddons.features.NonCategory
import co.stellarskys.nyahirunaddons.features.general.nonCategory.commandKeys.Main
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.handlers.Signal.fakeMessage
import co.stellarskys.stella.api.handlers.Signal.sendCommand
import co.stellarskys.stella.events.core.KeyEvent
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature

@Module
object CommandKeys : Feature("NonCategory.CommandKeys") {
    private val cooldownTicks = HashMap<String, Int>()

    override fun initialize() {
        on<TickEvent.Client> {
            if (!NonCategory.enabledCooldown) return@on
            for (entry in cooldownTicks.entries) {
                if (entry.value > 0) {
                    val value = entry.value - 1
                    entry.setValue(value)
                } else if (entry.value == 0) {
                    cooldownTicks.entries.remove(entry)
                }
            }
        }

        on<KeyEvent.Press> { event ->
            if (!NonCategory.commandKeys) return@on
            val keyCode = event.keyCode
            for (entry in Main().commandConfig) {
                if (keyCode == GuiUtils.keyMap[entry.key]) {
                    if ((cooldownTicks[entry.id] ?: 0) > 0) {
                        if (NonCategory.cooldownMessage) fakeMessage("§b[Ny]§r On Cooldown ${cooldownTicks[entry.id]} ticks")
                        return@on
                    }
                    val command = entry.command
                    if (command.isEmpty()) return@on
                    sendCommand(command)
                    if (!NonCategory.enabledCooldown) return@on
                    cooldownTicks[entry.id] = entry.cooldownTicks
                }
            }
        }
    }

    override fun onUnregister() {
        cooldownTicks.clear()
    }
}