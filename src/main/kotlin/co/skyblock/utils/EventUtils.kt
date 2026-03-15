package co.skyblock.utils

import java.util.UUID

data class ReadOnly(
    val jobs: MutableList<String> = mutableListOf("Archer", "Berserk", "Mage", "Tank", "Healer"),
    val dungeonPrefix: String = "Dungeon:",
    val floorPrefix: String = "Floor:",
    val membersPrefix: String = "Members:",
    val regexJoin: Regex = Regex("""Party Finder > (\w+) joined the dungeon group!"""),
    val regexPFEnter: Regex = Regex("""(\w+) entered (?:MM )?The Catacombs, Floor (\w+)"""),
    val leftPartyTrigger: String = "You left the party.",
    val removePartyTrigger: String = "Party Finder > Your group has been removed from the party finder!",
    val disbandPartyTrigger: String = "The party was disbanded because all invites expired and the party was empty."
)

data class CanWrite(
    var currentDungeon: String = "",
    var currentFloor: String = "",
    var currentMember: String = "",
    var hasDungeon: Boolean = false,
    var hasFloor: Boolean = false,
    var hasMember: Boolean = false,

    var inQueue: Boolean = false,
    var canAutoKick: Boolean = false,
    var lastMembers: Set<UUID> = emptySet(),

    var useFloor: String = "",
    var useFloorNumber: String = "",
    var queueFloor: String = "",
    var queueFloorNumber: String = "",
    var canUpdateFloor: Boolean = true
)

object EventUtils {
    val readOnly = ReadOnly()
    val canWrite = CanWrite()
}

