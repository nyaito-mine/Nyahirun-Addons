package co.skyblock.utils

data class ListEntry(
    val enabled: () -> Boolean,
    val stringFirst: String,
    val stringSecond: String = "",
    val stringThird: String = "",
    val stringFourth: String = "",
    val stringFifth: String = "",
)

data class ListEntryStringInt(
    val enabled: () -> Boolean,
    val string: String,
    val int: Int = 0,
)

data class ListEntryStringColorInt(
    val string: String,
    val color: Int = 0xFF404040.toInt()
)