package co.stellarskys.nyahirunaddons.config

import co.stellarskys.nyahirunaddons.NyahirunAddons
import co.stellarskys.nyahirunaddons.features.FeatureConfig
import co.stellarskys.stella.api.config.core.Config
import co.stellarskys.stella.utils.config as stellaConfig

val config = Config(NyahirunAddons.NAMESPACE) { }
val sections = FeatureConfig.register(config)

private val addonConfigBootstrap = run {
    stellaConfig.load()
    bindAddonSections(sections)
    attachSync(config, stellaConfig, sections)
    watchConfigClose(config, stellaConfig)
    config.load()
}
