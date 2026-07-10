package co.stellarskys.nyahirunaddons.commands

import co.stellarskys.nyahirunaddons.config.config
import co.stellarskys.nyahirunaddons.features.general.nonCategory.commandKeys.CommandKeysScreen
import co.stellarskys.nyahirunaddons.features.general.nonCategory.commandKeys.Page
import co.stellarskys.stella.annotations.Command
import co.stellarskys.stella.api.handlers.Atlas
import co.stellarskys.stella.api.handlers.Chronos
import co.stellarskys.stella.api.zenith.client

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
                    CommandKeysScreen.open()
                }
            }
        }
    }

    override fun isEnabled(): Boolean = true
}
