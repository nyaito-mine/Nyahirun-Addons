package co.stellarskys.nyahirunaddons.features.dungeon.partyFinder

import co.stellarskys.nyahirunaddons.api.hypixel.HypixelApiOnAddons
import co.stellarskys.nyahirunaddons.events.core.ItemTooltipEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.dungeons.Dungeon
import co.stellarskys.stella.api.handlers.Chronos.millis
import co.stellarskys.stella.api.hypixel.HypixelApi
import co.stellarskys.stella.features.Feature
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import kotlin.time.Duration.Companion.minutes

@Module
object Info : Feature("PartyFinder") {
    val Jobs = listOf("Archer", "Berserk", "Mage", "Tank", "Healer")
    val DungeonFloor = mutableListOf<Pair<String, String>>()
    var Class = ""
    private val FoundClasses: MutableSet<String> = mutableSetOf()
    private val ProcessedPlayers = mutableSetOf<String>()

    override fun initialize() {
        on<ItemTooltipEvent.Lines> { event ->
            val lines = event.lines
            val linesString = lines.toString()
            FoundClasses.clear()
            if (lines[3].stripped.startsWith("Currently Selected: ")) {
                Class = lines[3].stripped.removePrefix("Currently Selected: ").trim()
            }
            if (
                linesString.contains("Dungeon: ") &&
                linesString.contains("Floor: ") &&
                linesString.contains("Members: ")
                ) {
                DungeonFloor.add(Pair(lines[2].stripped.removePrefix("Dungeon: ").trim(), lines[2].stripped.removePrefix("Floor: ").trim()))
                for (i in lines.indices) {
                    val stripped = lines[i].stripped
                    var playerName = ""
                    for (job in Jobs) {
                        if (stripped.contains(job)) {
                            playerName = stripped.split(":")[0].trim()
                            FoundClasses.add(job)
                            break
                        }
                    }
                    if (playerName.isEmpty()) continue
                    if (playerName.lowercase() in ProcessedPlayers) {
                        lines[i] = lines[i].copy().append(
                            Component.literal(" §7(Loading...)")
                        )
                    }
                    if (!ProcessedPlayers.add(playerName.lowercase())) continue

                    HypixelApi.getUuid(playerName) { uuid ->
                        if (uuid == null) {
                            ProcessedPlayers.remove(playerName.lowercase())
                            return@getUuid
                        }
                        HypixelApiOnAddons.fetchSkyblockProfile(uuid, 10.minutes.millis) { profile ->
                            if (profile == null) {
                                ProcessedPlayers.remove(playerName.lowercase())
                                return@fetchSkyblockProfile
                            }
                            with(profile.dungeons) {
                                val floor = DungeonFloor.firstOrNull()
                                if (floor == null) {
                                    ProcessedPlayers.remove(playerName.lowercase())
                                    return@fetchSkyblockProfile
                                }
                                val dungeonType =
                                    if (floor.first == "The Catacombs") dungeonTypes.catacombs
                                    else dungeonTypes.mastermode

                                val cata = "%.1f".format(Dungeon.calculateDungeonLevel(dungeonTypes.catacombs.experience))
                                val secrets = secrets
                                val secretAverage = if (secrets == 0L) 0.0 else "%.1f".format(averageSecrets)
                                val pb = dungeonType.fastestSPlus[convertFloorToNumber(floor.second)]?.toLong()?.toMMSS() ?: "§eNo S+"

                                lines[i] = lines[i].copy().append(
                                    Component.literal(
                                        " §b(§6${cata}§r§b) " +
                                                "§8[§r§a${secrets}§r§8/§r§b${secretAverage}§r§8] " +
                                                "§r§8[§r§9${pb}§r§8]§r"
                                    )
                                )

                                ProcessedPlayers.remove(playerName.lowercase())
                            }
                        }
                    }
                }

                val missingJobs = Jobs.toMutableSet()
                missingJobs.removeAll(FoundClasses)

                if (missingJobs.isNotEmpty()) {
                    lines.add(
                        Component.literal(
                            "§e§lMissing: §r§f${missingJobs.joinToString(", ")}"
                        )
                    )
                }
            }
        }
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

    private fun Long.toMMSS(): String = if (this <= 0) "§eNo S+" else
        "%d:%02d.%d".format(this / 60000, (this % 60000) / 1000, (this % 1000) / 100)
}