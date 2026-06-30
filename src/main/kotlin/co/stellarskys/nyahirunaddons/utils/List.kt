package co.stellarskys.nyahirunaddons.utils

data class ListEntry(
    val enabled: () -> Boolean,
    val stringFirst: String,
    val stringSecond: String = "",
    val stringThird: String = "",
    val stringFourth: String = "",
    val stringFifth: String = "",
) {
    val triggers: List<String> by lazy(LazyThreadSafetyMode.NONE) {
        listOf(stringFirst, stringSecond, stringThird, stringFourth, stringFifth)
            .filter(String::isNotBlank)
    }
}

data class ListEntryStringInt(
    val enabled: () -> Boolean,
    val string: String,
    val int: Int = 0,
)

data class ListEntryStringColorInt(
    val string: String,
    val color: Int = 0xFF404040.toInt()
)
