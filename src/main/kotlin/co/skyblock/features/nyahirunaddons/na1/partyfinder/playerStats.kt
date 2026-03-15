package co.skyblock.features.nyahirunaddons.na1.partyfinder

import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.features.Feature
import co.skyblock.events.core.ItemTooltipEvent
import co.skyblock.events.core.PartyFinderEvent
import co.skyblock.utils.ApiResultsProvider.getAPIResults
import co.skyblock.utils.EventUtils
import co.skyblock.utils.dungeon.api.Manager
import dev.deftu.omnicore.api.client.client
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object playerStats : Feature("playerStats") {

    private val canWrite = EventUtils.canWrite
    private val readOnly = EventUtils.readOnly

    override fun initialize() {
        on<ItemTooltipEvent.Line> { event ->
            if (!canWrite.canUpdateFloor) return@on
            val lines = event.lines

            canWrite.hasDungeon = false
            canWrite.hasFloor = false
            canWrite.hasMember = false

            for (i in lines.indices) {

                val line = lines[i]
                val str = line.string

                when {
                    str.startsWith("Dungeon:") -> {
                        canWrite.hasDungeon = true
                    }

                    str.startsWith("Floor:") -> {
                        canWrite.hasFloor = true
                        canWrite.currentFloor = str.removePrefix("Floor:").trim()
                    }

                    str.startsWith("Members:") -> {
                        canWrite.hasMember = true
                    }
                }
            }

            if (canWrite.hasDungeon && canWrite.hasFloor && canWrite.hasMember) {
                canWrite.useFloor = convertFloorToKey(canWrite.currentFloor)
                canWrite.useFloorNumber = convertFloorToNumber(canWrite.currentFloor)
            }
        }

        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            if (msg == readOnly.leftPartyTrigger || msg == readOnly.removePartyTrigger || msg == readOnly.disbandPartyTrigger) canWrite.canUpdateFloor = true

            readOnly.regexJoin.find(msg)?.let {
                val name = it.groupValues[1]
                val clientName = client.player?.name?.stripped.toString()
                if (name == clientName) return@on
                getAPIAndChat(name)
            }
        }

        on<PartyFinderEvent.Queue> { event ->
            canWrite.inQueue = true
            val floor = event.floor.toString()
            canWrite.queueFloor = convertFloorToKey(floor)
            canWrite.queueFloorNumber = convertFloorToNumber(floor)
        }

        on<PartyFinderEvent.Leave> {
            canWrite.inQueue = false
        }

        on<PartyFinderEvent.PartyInfo> {event ->
            val inParty = event.inParty
            if (inParty) canWrite.canUpdateFloor = false
        }
    }

    private fun getAPIAndChat(name: String) {
        var floor: String
        var floorNumber: String
        if (canWrite.inQueue) {
            floor = canWrite.queueFloor
            floorNumber = canWrite.queueFloorNumber
        } else {
            floor = canWrite.useFloor
            floorNumber = canWrite.useFloorNumber
        }
        if (floor.isBlank()) return

        val header = Component.literal("━━━━━━━━━━ $name Stats ━━━━━━━━━━")
            .withStyle { style ->
                style.withHoverEvent(
                    HoverEvent.ShowText(
                        Component.literal("Nyahirun Addons")
                    )
                )
            }
        val kick = Component.literal("§cClick to Kick")
            .withStyle { style ->
                style.withClickEvent(
                    ClickEvent.RunCommand("/party kick $name")
                )
            }
        val reinvite = Component.literal("§aClick to Re Invite")
            .withStyle { style ->
                style.withClickEvent(
                    ClickEvent.RunCommand("/party invite $name")
                )
            }

        var apis = getAPIResults(name)
        val floorPBStr = Manager.getCachedPBString(name, "The Catacombs", floor)
        val masterPBStr = Manager.getCachedPBString(name, "Master Mode The Catacombs", floor)

        if (apis.cataLevel == null) {
            client.player?.displayClientMessage(
                Component.literal("§e[NA] Debug -> Player Stats cataLevel Null getAPI"),
                false
            )
            if (!Manager.hasValidCache(name) || !Manager.isFetching(name)) {
                Manager.fetchAsync(name) {
                    apis = getAPIResults(name)
                    val newfloorPBStr = Manager.getCachedPBString(name, "The Catacombs", floor)
                    val newMasterPBStr = Manager.getCachedPBString(name, "Master Mode The Catacombs", floor)

                    client.execute {
                        val message = header
                            .append("\n")
                            .append(Component.literal("§fLevel ${apis.sbLevel} §fCata §b${apis.cataLevel} §fMP §d${apis.magicalPower}\n"))
                            .append(Component.literal("§fPB §2F${floorNumber} ${newfloorPBStr} §f| §4M${floorNumber} ${newMasterPBStr}\n"))
                            .append(Component.literal("§fSecret §e${apis.secrets}§f(§6${apis.secretAverage}§f/Run)\n"))
                            .append(kick)
                            .append(Component.literal("§f/"))
                            .append(reinvite)

                        client.player?.displayClientMessage(message, false)
                    }
                }
            }
            return
        }

        val message = header
            .append("\n")
            .append(Component.literal("§fLevel ${apis.sbLevel} §fCata §b${apis.cataLevel} §fMP §d${apis.magicalPower}\n"))
            .append(Component.literal("§fPB §2F${floorNumber} ${floorPBStr} §f| §4M${floorNumber} ${masterPBStr}\n"))
            .append(Component.literal("§fSecret §e${apis.secrets}§f(§6${apis.secretAverage}§f/Run)\n"))
            .append(kick)
            .append(Component.literal("§f/"))
            .append(reinvite)

        client.player?.displayClientMessage(message, false)
    }

    private fun convertFloorToKey(floorDisplay: String): String =
        when (floorDisplay) {
            "Entrance", "E" -> "floor_0"
            "Floor I", "F1", "M1" -> "floor_1"
            "Floor II", "F2", "M2" -> "floor_2"
            "Floor III", "F3", "M3" -> "floor_3"
            "Floor IV", "F4", "M4" -> "floor_4"
            "Floor V", "F5", "M5" -> "floor_5"
            "Floor VI", "F6", "M6" -> "floor_6"
            "Floor VII", "F7", "M7" -> "floor_7"
            else -> ""
        }

    private fun convertFloorToNumber(floorDisplay: String): String =
        when (floorDisplay) {
            "Entrance", "E" -> "0"
            "Floor I", "F1", "M1" -> "1"
            "Floor II", "F2", "M2" -> "2"
            "Floor III", "F3", "M3" -> "3"
            "Floor IV", "F4", "M4" -> "4"
            "Floor V", "F5", "M5" -> "5"
            "Floor VI", "F6", "M6" -> "6"
            "Floor VII", "F7", "M7" -> "7"
            else -> ""
        }
}