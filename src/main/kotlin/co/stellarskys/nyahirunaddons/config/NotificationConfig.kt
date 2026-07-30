package co.stellarskys.nyahirunaddons.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import net.fabricmc.loader.api.FabricLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.io.path.exists

data class NotificationEntry(
    val id: String = UUID.randomUUID().toString(),
    val enabled: Boolean = true,
    val notification: String = "",
    val trigger: String = "",
    val displayTicks: Int = 40,
    val sound: String = "entity.experience_orb.pickup",
    val soundVolume: Float = 1.0f,
    val soundPitch: Float = 0.5f
)

object NotificationConfig {
    private const val CONFIG_DIRECTORY_NAME = "nyahirun-addons"
    private const val FILE_NAME = "notification.json"
    private const val CURRENT_SCHEMA_VERSION = 1

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configDirectoryPath: Path = FabricLoader.getInstance().configDir.resolve(CONFIG_DIRECTORY_NAME)
    private val configPath: Path = configDirectoryPath.resolve(FILE_NAME)
    private val legacyConfigPath: Path = FabricLoader.getInstance().configDir.resolve(FILE_NAME)

    private data class NotificationFile(
        val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
        val entries: List<NotificationEntry> = emptyList()
    )

    private var cachedEntries: MutableList<NotificationEntry>? = null

    fun getEntries(): List<NotificationEntry> {
        return getOrLoadEntries().map { it.copy() }
    }

    fun update(transform: (MutableList<NotificationEntry>) -> Unit) {
        val updated = getOrLoadEntries().toMutableList()
        transform(updated)
        val normalized = normalize(updated)
        saveEntries(normalized)
        cachedEntries = normalized.toMutableList()
    }

    private fun getOrLoadEntries(): MutableList<NotificationEntry> {
        cachedEntries?.let { return it }
        val loaded = readEntries().toMutableList()
        cachedEntries = loaded
        return loaded
    }

    private fun readEntries(): List<NotificationEntry> {
        prepareConfigDirectory()
        migrateLegacyFileIfNeeded()

        if (!configPath.exists()) return emptyList()

        val content = runCatching {
            Files.readString(configPath, StandardCharsets.UTF_8)
        }.getOrDefault("")

        if (content.isBlank()) return emptyList()

        val parsed = runCatching {
            gson.fromJson(content, NotificationFile::class.java)
        }.getOrElse { error ->
            if (error is JsonSyntaxException) {
                backupBrokenFile()
            }
            return emptyList()
        } ?: return emptyList()

        return normalize(parsed.entries)
    }

    private fun saveEntries(entries: List<NotificationEntry>) {
        prepareConfigDirectory()
        migrateLegacyFileIfNeeded()

        val json = gson.toJson(
            NotificationFile(
                schemaVersion = CURRENT_SCHEMA_VERSION,
                entries = entries
            )
        )

        Files.writeString(configPath, json, StandardCharsets.UTF_8)
    }

    private fun normalize(entries: List<NotificationEntry>): List<NotificationEntry> {
        return entries.map { raw ->
            val id = raw.id.trim().ifEmpty { UUID.randomUUID().toString() }

            NotificationEntry(
                id = id,
                enabled = raw.enabled,
                notification = raw.notification,
                trigger = raw.trigger,
                displayTicks = raw.displayTicks,
                sound = raw.sound,
                soundVolume = raw.soundVolume,
                soundPitch = raw.soundPitch
            )
        }
    }

    private fun prepareConfigDirectory() {
        Files.createDirectories(configDirectoryPath)
    }

    private fun migrateLegacyFileIfNeeded() {
        if (Files.exists(configPath)) return
        if (!Files.exists(legacyConfigPath)) return

        Files.move(legacyConfigPath, configPath)
    }

    private fun backupBrokenFile() {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val brokenPath = configDirectoryPath.resolve("$FILE_NAME.broken-$timestamp")
        runCatching {
            Files.move(configPath, brokenPath)
        }
    }
}

