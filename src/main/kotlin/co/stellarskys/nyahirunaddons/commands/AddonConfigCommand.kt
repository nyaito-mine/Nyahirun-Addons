package co.stellarskys.nyahirunaddons.commands

import co.stellarskys.nyahirunaddons.config.config
import co.stellarskys.nyahirunaddons.config.gui.Screen
import co.stellarskys.stella.annotations.Command
import co.stellarskys.stella.api.handlers.Atlas
import co.stellarskys.stella.api.handlers.Chronos

@Command
object AddonConfigCommand : Atlas("nyahirunaddons", "nyahirun", "ny") {
    init {
        runs {
            config.load()
            config.open()
        }
        literal("commandkeys") {
            runs {
                Chronos.Tick post {
                    Screen.open("CommandKeys")
                }
            }
        }
        literal("notification") {
            runs {
                Chronos.Tick post {
                    Screen.open("Notification")
                }
            }
        }
    }

    override fun isEnabled(): Boolean = true
}
