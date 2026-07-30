package co.stellarskys.nyahirunaddons.utils

import java.util.UUID

data class ReadOnly(
    val dungeonStartTrigger: String = "Here, I found this map when I first entered the dungeon.",

    val P1StartTrigger: String = "[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!",
    val P1EnragedTrigger: String = "⚠ Maxor is enraged! ⚠",
    val P1EndTrigger: String = "[BOSS] Maxor: I'M TOO YOUNG TO DIE AGAIN!",
    val P2StartTrigger: String = "[BOSS] Storm: Pathetic Maxor, just like expected.",
    val P2ThunderTrigger: String = "[BOSS] Storm: THUNDER LET ME BE YOUR CATALYST!",
    val P2EnragedTrigger: String = "⚠ Storm is enraged! ⚠",
    val P2EndTrigger: String = "[BOSS] Storm: I should have known that I stood no chance.",
    val P3StartTrigger: String = "[BOSS] Goldor: Who dares trespass into my domain?",
    val P3GoldorTrigger: String = "The Core entrance is opening!",
    val P3EndTrigger: String = "[BOSS] Necron: You went further than any human before, congratulations",
    val P4StartTrigger: String = "[BOSS] Necron: I'm afraid, your journey ends now.",
    val P4DropTrigger: String = "[BOSS] Necron: Goodbye.",
    val P4EndTrigger: String = "[BOSS] Necron: All this, for nothing...",
    val P5RagnarockTrigger: String = "I no longer wish to fight, but I know that will not stop you.",

    val jobs: MutableList<String> = mutableListOf("Archer", "Berserk", "Mage", "Tank", "Healer"),
    val currentlySelectedPrefix: String = "Currently Selected:",
    val dungeonPrefix: String = "Dungeon:",
    val floorPrefix: String = "Floor:",
    val membersPrefix: String = "Members:",
    //Party Finder > MidNyaitoDye joined the dungeon group! (Healer Level 46)
    val regexJoin: Regex = Regex("""Party Finder > (\w+) joined the dungeon group! \((\w+) Level (\d+)\)"""),
    //[357] MidNyaitoDye 🌺 (Mage XLIII)
    val regexPFEnter: Regex = Regex("""(\w+) entered (?:MM )?The Catacombs, Floor (\w+)"""),
    val regexClientNameTab: Regex = Regex("""\[(\d+)] (\w+) (?:.+\s)?\((\w+) (\w+)\)"""),
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
    var canUpdateFloor: Boolean = true,

    var isClass: String = "",
)

data class Map(
    val playerClass: MutableMap<String, String> = mutableMapOf(),
    val foundJobs: MutableSet<String> = mutableSetOf(),
)

object EventUtils {
    val ReadOnly = ReadOnly()
    val CanWrite = CanWrite()
    val Map = Map()
}

