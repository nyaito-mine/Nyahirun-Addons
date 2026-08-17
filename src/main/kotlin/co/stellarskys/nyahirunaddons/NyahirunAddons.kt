package co.stellarskys.nyahirunaddons

import co.stellarskys.nyahirunaddons.config.config as addonConfig
import co.stellarskys.nyahirunaddons.features.FeatureConfig
import co.stellarskys.stella.api.horizon.animation.DeltaTracker
import co.stellarskys.stella.api.zenith.Zenith.client
import co.stellarskys.stella.utils.config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import co.stellarskys.stella.utils.config as stellaConfig
import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object NyahirunAddons : ClientModInitializer {
    @JvmStatic val LOGGER: Logger = LogManager.getLogger("nyahirun-addons")
    @JvmStatic val NAMESPACE: String = "nyahirun-addons"
    @JvmStatic val PREFIX: String = "§7[§bNyahirun§7]"
    @JvmStatic val SHORTPREFIX: String = "§b[Ny]§r"
    @JvmStatic val API: String = "https://nyahirun-api.nyahirunaddons.workers.dev"
    @JvmStatic val PATH: String get() = config.path
    @JvmStatic val DELTA: DeltaTracker = DeltaTracker()
    @JvmStatic val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @JvmStatic val partialTicks: Float get() = client.deltaTracker.getGameTimeDeltaPartialTick(true)

    override fun onInitializeClient() {
        FeatureConfig.bindSharedSections(FeatureConfig.register(stellaConfig))
        addonConfig.load()
    }
}
