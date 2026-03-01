package co.skyblock.features.nyahirunaddons.na1.partyfinder

import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.features.Feature
import co.skyblock.events.core.ItemTooltipEvent
import co.skyblock.events.core.PartyFinderEvent
import co.skyblock.utils.dungeon.api.Manager
import dev.deftu.omnicore.api.client.client
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object playerStats : Feature("playerStats") {

    private var useFloor = ""
    private var useFloorNumber = ""

    private var inQueue = false
    private var queueFloor = ""
    private var queueFloorNumber = ""

    private var canUpdateFloor = true

    private val leftPartyTrigger = "you left the party."
    private val removePartyTrigger = "party finder > your group has been removed from the party finder!"
    private val disbandPartyTrigger = "the party was disbanded because all invites expired and the party was empty."
    private val regexJoin =
        Regex("""Party Finder > (\w+) joined the dungeon group!""")

    override fun initialize() {
        on<ItemTooltipEvent.Line> { event ->
            if (!canUpdateFloor) return@on
            val lines = event.lines
            var currentFloor = ""
            var hasDungeon = false
            var hasFloor = false
            var hasMember = false

            for (i in lines.indices) {

                val line = lines[i]
                val str = line.string

                when {
                    str.startsWith("Dungeon:") -> {
                        hasDungeon = true
                    }

                    str.startsWith("Floor:") -> {
                        hasFloor = true
                        currentFloor = str.removePrefix("Floor:").trim()
                    }

                    str.startsWith("Members:") -> {
                        hasMember = true
                    }
                }
            }

            if (hasDungeon && hasFloor && hasMember) {
                useFloor = convertFloorToKey(currentFloor)
                useFloorNumber = convertFloorToNumber(currentFloor)
            }
        }

        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            if (msg == leftPartyTrigger || msg == removePartyTrigger || msg == disbandPartyTrigger) canUpdateFloor = true

            regexJoin.find(msg)?.let {
                val name = it.groupValues[1]
                val clientName = client.player?.name?.stripped.toString()
                if (name == clientName) return@on
                getAPIAndChat(name)
            }
        }

        on<PartyFinderEvent.Queue> { event ->
            inQueue = true
            val floor = event.floor.toString()
            queueFloor = convertFloorToKey(floor)
            queueFloorNumber = convertFloorToNumber(floor)
        }

        on<PartyFinderEvent.Leave> {
            inQueue = false
        }

        on<PartyFinderEvent.PartyInfo> {event ->
            val inParty = event.inParty
            if (inParty) canUpdateFloor = false
        }
    }

    private fun getAPIAndChat(name: String) {
        var floor: String
        var floorNumber: String
        if (inQueue) {
            floor = queueFloor
            floorNumber = queueFloorNumber
        } else {
            floor = useFloor
            floorNumber = useFloorNumber
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

        val cataLevel = Manager.getCachedCataLevel(name)
        val secrets = Manager.getCachedSecret(name)
        val secretAverage = Manager.getCachedSecretAve(name)
        val floorPBStr = Manager.getCachedPBString(name, "The Catacombs", floor)
        val masterPBStr = Manager.getCachedPBString(name, "Master Mode The Catacombs", floor)
        val magicalPower = Manager.getCachedMagicalPower(name)
        val sbLevel = Manager.getCachedSBLevel(name)

        if (cataLevel == null) {
            if (!Manager.isFetching(name)) {
                Manager.fetchAsync(name) {
                    val newCataLevel = Manager.getCachedCataLevel(name)
                    val newSecrets = Manager.getCachedSecret(name)
                    val newSecretAverage = Manager.getCachedSecretAve(name)
                    val newFloorPBStr = Manager.getCachedPBString(name, "The Catacombs", floor)
                    val newMasterPBStr = Manager.getCachedPBString(name, "Master Mode The Catacombs", floor)
                    val newMagicalPower = Manager.getCachedMagicalPower(name)
                    val newSBLevel = Manager.getCachedSBLevel(name)
                    client.execute {
                        val message = header
                            .append("\n")
                            .append(Component.literal("§fLevel ${newSBLevel} §fCata §b${newCataLevel} §fMP §d${newMagicalPower}\n"))
                            .append(Component.literal("§fPB §2F${floorNumber} ${newFloorPBStr} §f| §4M${floorNumber} ${newMasterPBStr}\n"))
                            .append(Component.literal("§fSecret §e${newSecrets}§f(§6${newSecretAverage}§f/Run)\n"))
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
            .append(Component.literal("§fLevel ${sbLevel} §fCata §b${cataLevel} §fMP §d${magicalPower}\n"))
            .append(Component.literal("§fPB §2F${floorNumber} ${floorPBStr} §f| §4M${floorNumber} ${masterPBStr}\n"))
            .append(Component.literal("§fSecret §e${secrets}§f(§6${secretAverage}§f/Run)\n"))
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