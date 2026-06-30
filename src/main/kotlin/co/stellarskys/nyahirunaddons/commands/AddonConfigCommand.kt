package co.stellarskys.nyahirunaddons.commands

import co.stellarskys.nyahirunaddons.config.config
import co.stellarskys.stella.annotations.Command
import co.stellarskys.stella.api.handlers.Atlas

@Command
object AddonConfigCommand : Atlas("nyahirunaddons", "nyahirun", "nas") {
    init {
        runs {
            config.load()
            config.open()
        }
    }

    override fun isEnabled(): Boolean = true
}
