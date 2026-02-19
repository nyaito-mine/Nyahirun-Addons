package co.skyblock.features.nyahirunaddons.na1.general

import co.skyblock.events.core.ItemTooltipEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.network.chat.Component
import co.skyblock.utils.dungeon.api.Manager
import co.skyblock.utils.dungeon.api.PlayerUtils
import java.awt.Color

@Module
object partyFinder : Feature("partyFinder") {

    val partyFinderConfig by config.property<Int>("partyFinder")
    val partyFinderHighlightCanJoinConfig by config.property<Color>("partyFinderHighlightCanJoin")
    val partyFinderHighlightCantJoinConfig by config.property<Color>("partyFinderHighlightCantJoin")

    override fun initialize() {

        val jobs = listOf("Archer", "Berserk", "Mage", "Tank", "Healer")

        on<ItemTooltipEvent.Line> { event ->

            if (partyFinderConfig == 0 || partyFinderConfig == 2) return@on

            val lines = event.lines
            var currentDungeon = ""
            var currentFloor = ""
            var hasDungeon = false
            var hasNote = false

            val foundJobs = mutableSetOf<String>()

            for (i in lines.indices) {

                val line = lines[i]
                val str = line.string

                when {
                    str.startsWith("Dungeon:") -> {
                        currentDungeon = str.removePrefix("Dungeon:").trim()
                        hasDungeon = true
                    }

                    str.startsWith("Note:") -> {
                        hasNote = true
                    }

                    str.startsWith("Floor:") -> {
                        currentFloor = str.removePrefix("Floor:").trim()
                    }

                    str.contains(":") -> {

                        for (job in jobs) {
                            if (str.contains(": $job") && hasDungeon && hasNote) {

                                foundJobs.add(job)

                                val playerName = PlayerUtils.extractPlayerName(str)

                                val cataLevel = Manager.getCachedLevel(playerName)
                                val secrets = Manager.getCachedSecret(playerName)
                                val secretAverage = Manager.getCachedSecretAve(playerName)

                                val floorKey = convertFloorToKey(currentFloor)
                                val pbStr = Manager.getCachedPBString(
                                    playerName,
                                    currentDungeon,
                                    floorKey
                                )

                                if (cataLevel != null) {

                                    lines[i] = line.copy().append(
                                        Component.literal(
                                            " §b(§6$cataLevel§r§b) " +
                                                    "§8[§r§a$secrets§r§8/§r§b$secretAverage§r§8] " +
                                                    "§r§8[§r§9${pbStr ?: "?"}§r§8]§r"
                                        )
                                    )

                                } else {

                                    if (!Manager.isFetching(playerName)) {
                                        Manager.fetchAsync(playerName)
                                    }

                                    lines[i] = line.copy().append(
                                        Component.literal(" §7(Loading...)")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (hasDungeon && hasNote) {
                val missingJobs = jobs.toMutableSet()
                missingJobs.removeAll(foundJobs)

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

    private fun convertFloorToKey(floorDisplay: String): String =
        when (floorDisplay) {
            "Entrance" -> "floor_0"
            "Floor I" -> "floor_1"
            "Floor II" -> "floor_2"
            "Floor III" -> "floor_3"
            "Floor IV" -> "floor_4"
            "Floor V" -> "floor_5"
            "Floor VI" -> "floor_6"
            "Floor VII" -> "floor_7"
            else -> ""
        }
}
