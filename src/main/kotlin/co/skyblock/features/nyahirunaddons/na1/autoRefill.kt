package co.skyblock.features.nyahirunaddons.na1

import co.skyblock.utils.ListEntryStringInt
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.client.Minecraft
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object autoRefill : Feature("autoRefill", island = SkyBlockIsland.THE_CATACOMBS) {

    val enderPearlConfig by config.property<Boolean>("autoRefillEnderPearl")
    val superboomTNTConfig by config.property<Boolean>("autoRefillSuperboomTNT")
    val spiritLeapConfig by config.property<Boolean>("autoRefillSpiritLeap")
    val inflatableJerryConfig by config.property<Boolean>("autoRefillInflatableJerry")
    val decoyConfig by config.property<Boolean>("autoRefillDecoy")

    private val autoRefillConfigs = listOf(
        ListEntryStringInt({ enderPearlConfig }, "Ender Pearl", 16),
        ListEntryStringInt({ superboomTNTConfig }, "Superboom TNT", 64),
        ListEntryStringInt({ spiritLeapConfig }, "Spirit Leap", 16),
        ListEntryStringInt({ inflatableJerryConfig }, "Inflatable Jerry", 64),
        ListEntryStringInt({ decoyConfig }, "Decoy", 64)
    )

    private val trigger = "Here, I found this map when I first entered the dungeon."

    private val commandQueue = ArrayDeque<String>()
    private var waitTicks = 0

    override fun initialize() {
        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            val client = Minecraft.getInstance()
            if (client.player == null) return@on
            if (!msg.contains(trigger)) return@on

            for (entry in autoRefillConfigs) {
                val enabled = entry.enabled()
                if (!enabled) continue

                val itemName = entry.string
                val itemMax = entry.int
                val itemCount = countItemByName(client, itemName)

                if (itemCount < itemMax) {
                    val need = itemMax - itemCount
                    commandQueue.add("gfs $itemName $need")
                }
            }

            return@on
        }

        on<TickEvent.Client> {
            val client = Minecraft.getInstance()
            if (client.player == null) return@on

            if (waitTicks-- > 0) return@on

            if (commandQueue.isNotEmpty()) {
                sendCommand(client, commandQueue.removeFirst())
                waitTicks = 40
            }
        }
    }

    private fun countItemByName(client: Minecraft, targetName: String): Int {
        var count = 0
        for (stack in client.player!!.getInventory().nonEquipmentItems) {
            if (stack.isEmpty) continue
            val itemName = stack.hoverName.string
            if (itemName.contains(targetName)) {
                count += stack.count
            }
        }
        return count
    }

    private fun sendCommand(client: Minecraft, command: String) {
        client.player!!.connection.sendCommand(command)
    }
}
