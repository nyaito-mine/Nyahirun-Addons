package co.skyblock

import co.skyblock.events.EventBusAddons
import co.skyblock.utils.ConfigAddons
import net.fabricmc.api.ClientModInitializer
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

class NyahirunAddons : ClientModInitializer {

    override fun onInitializeClient() {
        ConfigAddons
        EventBusAddons

        val NA1FeatureList = listOf(
            co.skyblock.features.nyahirunaddons.na1.general.partyFinder,
            co.skyblock.features.nyahirunaddons.na1.general.usePetHighlight,
            co.skyblock.features.nyahirunaddons.na1.general.efficientDB,

            co.skyblock.features.nyahirunaddons.na1.autoRefill,

            co.skyblock.features.nyahirunaddons.na1.renderHighlight.dropItem,
            co.skyblock.features.nyahirunaddons.na1.renderHighlight.wither,
            co.skyblock.features.nyahirunaddons.na1.renderHighlight.mimicChest,

            co.skyblock.features.nyahirunaddons.na1.disableUse.secondSoulSand,
            co.skyblock.features.nyahirunaddons.na1.disableUse.placeTuba,
            co.skyblock.features.nyahirunaddons.na1.disableUse.sbMenu
            )

        val NA2FeatureList = listOf(
            co.skyblock.features.nyahirunaddons.na2.notification,

            co.skyblock.features.nyahirunaddons.na2.chatHider
            )

        /*
        ClientTickEvents.END_CLIENT_TICK.register { client ->
        if (client != null) {
        }
        }
        */
    }
}
