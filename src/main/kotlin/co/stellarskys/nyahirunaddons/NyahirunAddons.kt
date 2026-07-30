package co.stellarskys.nyahirunaddons

import co.stellarskys.nyahirunaddons.config.config as addonConfig
import co.stellarskys.nyahirunaddons.features.FeatureConfig
import co.stellarskys.stella.api.zenith.Zenith.client
import co.stellarskys.stella.utils.config as stellaConfig
import net.fabricmc.api.ClientModInitializer

object NyahirunAddons : ClientModInitializer {
    @JvmStatic val NAMESPACE: String = "nyahirun-addons"

    @JvmStatic val partialTicks: Float get() = client.deltaTracker.getGameTimeDeltaPartialTick(true)

    override fun onInitializeClient() {
        FeatureConfig.bindSharedSections(FeatureConfig.register(stellaConfig))
        addonConfig.load()
    }
}
