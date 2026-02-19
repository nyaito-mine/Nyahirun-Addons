package co.skyblock.utils.dungeon

object ClassState {
    private val CLASSES = mutableListOf<String?>("Archer", "Berserk", "Mage", "Tank", "Healer")

    private var selectedClass: String? = null

    @JvmStatic
    fun setSelectedClass(clazz: String?) {
        if (CLASSES.contains(clazz)) {
            selectedClass = clazz
        }
    }

    @JvmStatic
    fun getSelectedClass(): String? {
        return selectedClass
    }

    @JvmStatic
    fun hasSelectedClass(): Boolean {
        return selectedClass != null
    }
}
