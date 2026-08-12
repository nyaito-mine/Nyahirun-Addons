package co.stellarskys.nyahirunaddons.config

interface ConfigResponseEntry {
    val id: String
}

open class ListConfigResponse<ConfigEntry, Entry : ConfigResponseEntry>(
    private val getEntries: () -> List<ConfigEntry>,
    private val updateEntries: ((MutableList<ConfigEntry>) -> Unit) -> Unit,
    private val configIdOf: (ConfigEntry) -> String,
    private val toResponseEntry: (ConfigEntry) -> Entry,
    private val toConfigEntry: (Entry) -> ConfigEntry
) {
    fun getCommands(): List<Entry> {
        return getEntries().map(toResponseEntry)
    }

    fun updateCommand(entry: Entry) {
        updateEntries { entries ->
            val index = entries.indexOfFirst { configIdOf(it) == entry.id }
            if (index != -1) {
                entries[index] = toConfigEntry(entry)
            }
        }
    }

    fun addCommand(entry: Entry) {
        updateEntries { entries ->
            entries.add(toConfigEntry(entry))
        }
    }

    fun deleteCommand(id: String) {
        updateEntries { entries ->
            entries.removeIf { configIdOf(it) == id }
        }
    }
}
