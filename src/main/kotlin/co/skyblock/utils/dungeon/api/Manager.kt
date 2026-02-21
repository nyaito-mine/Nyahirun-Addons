package co.skyblock.utils.dungeon.api

import com.google.gson.JsonParser
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

object Manager {

    private const val CACHE_DURATION = 60 * 60 * 1000L // 1時間

    // ===== データ構造 =====

    data class PlayerData(
        val cataLevel: Double,
        val totalSecrets: Int,
        val secretAverage: Double,
        val floorPB: Map<String, String>,
        val masterFloorPB: Map<String, String>
    )

    private data class CachedPlayer(
        val data: PlayerData,
        val timestamp: Long
    )

    // ===== キャッシュ =====

    private val cache = ConcurrentHashMap<String, CachedPlayer>()
    private val uuidCache = ConcurrentHashMap<String, String>()
    private val fetching = ConcurrentHashMap.newKeySet<String>()

    // ===== 外部取得用 =====

    fun getCachedLevel(playerName: String): Double? =
        getValidCache(playerName)?.cataLevel

    fun getCachedSecret(playerName: String): Int? =
        getValidCache(playerName)?.totalSecrets

    fun getCachedSecretAve(playerName: String): Double? =
        getValidCache(playerName)?.secretAverage

    fun getCachedPBString(playerName: String, dungeon: String, floorKey: String): String? {
        val data = getValidCache(playerName) ?: return null
        return if (dungeon.contains("Master", ignoreCase = true)) {
            data.masterFloorPB[floorKey]
        } else {
            data.floorPB[floorKey]
        }
    }

    fun isFetching(playerName: String): Boolean =
        fetching.contains(playerName)

    fun fetchAsync(playerName: String) {
        if (fetching.contains(playerName)) return
        fetching.add(playerName)

        thread(name = "Nyahirun-API-$playerName") {
            try {
                val uuid = getUUID(playerName) ?: return@thread

                val url = URL("https://api.nyahirunaddons.workers.dev/?uuid=$uuid")
                val conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode != 200) return@thread

                InputStreamReader(conn.inputStream).use { reader ->
                    val root = JsonParser.parseReader(reader).asJsonObject

                    val cataLevel = root
                        .getAsJsonObject("Cata")
                        .get("CataLevel").asString.toDouble()

                    val secretObj = root.getAsJsonObject("Secret")
                    val totalSecrets = secretObj.get("TotalSecrets").asInt
                    val secretAverage =
                        secretObj.get("SecretAveragePerRun").asString.toDouble()

                    val floorPB = mutableMapOf<String, String>()
                    val masterPB = mutableMapOf<String, String>()

                    val floorJson = root
                        .getAsJsonObject("Floor")
                        .getAsJsonObject("PersonalBest")

                    for ((key, value) in floorJson.entrySet()) {
                        val obj = value.asJsonObject
                        floorPB["floor_$key"] = obj.get("plus").asString
                    }

                    val masterJson = root
                        .getAsJsonObject("MasterFloor")
                        .getAsJsonObject("PersonalBest")

                    for ((key, value) in masterJson.entrySet()) {
                        val obj = value.asJsonObject
                        masterPB["floor_$key"] = obj.get("masterplus").asString
                    }

                    val playerData = PlayerData(
                        cataLevel,
                        totalSecrets,
                        secretAverage,
                        floorPB,
                        masterPB
                    )

                    cache[playerName] = CachedPlayer(
                        playerData,
                        System.currentTimeMillis()
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                fetching.remove(playerName)
            }
        }
    }

    // ===== 内部処理 =====

    private fun getValidCache(playerName: String): PlayerData? {
        val cached = cache[playerName] ?: return null
        val now = System.currentTimeMillis()

        return if (now - cached.timestamp < CACHE_DURATION) {
            cached.data
        } else {
            cache.remove(playerName)
            null
        }
    }

    private fun getUUID(playerName: String): String? {
        uuidCache[playerName]?.let { return it }

        return try {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$playerName")
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode != 200) return null

            InputStreamReader(conn.inputStream).use { reader ->
                val json = JsonParser.parseReader(reader).asJsonObject
                val uuid = json.get("id").asString
                uuidCache[playerName] = uuid
                uuid
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
