package co.skyblock.utils.dungeon.api

import com.google.gson.JsonElement
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
        val selectedClass: String,
        val classLevel: Map<String, Double>,
        val totalSecrets: Int,
        val secretAverage: Double,
        val floorPB: Map<String, String>,
        val masterFloorPB: Map<String, String>,
        val magicalPower: Int,
        val sbLevel: String,
        val goldCollection: String,
        val skillLevel: Map<String, Double>,
        val pets: List<Pet>,
        val inventories: Inventories
    )

    private data class CachedPlayer(
        val data: PlayerData,
        val timestamp: Long
    )

    data class Pet(
        val name: String,
        val item: String,
        val rarity: String,
        val level: Int
    )

    data class Inventories(
        val inventory: String?,
        val enderchest: String?,
        val backpacks: Map<String, String>,
        val armor: String?,
        val wardrobe: String?,
        val wardrobeEquipped: Int?,
        val equipment: String?
    )

    // ===== キャッシュ =====

    private val cache = ConcurrentHashMap<String, CachedPlayer>()
    private val uuidCache = ConcurrentHashMap<String, String>()
    private val fetching = ConcurrentHashMap.newKeySet<String>()

    // ===== 外部取得用 =====

    fun getCachedCataLevel(playerName: String): Double? =
        getValidCache(playerName)?.cataLevel

    fun getCachedSelectedClass(playerName: String): String? =
        getValidCache(playerName)?.selectedClass

    fun getCachedClassLevel(playerName: String, className: String): Double? {
        val data = getValidCache(playerName) ?: return null
        return data.classLevel[className]
    }

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

    fun getCachedMagicalPower(playerName: String): Int? =
        getValidCache(playerName)?.magicalPower

    fun getCachedSBLevel(playerName: String): String? =
        getValidCache(playerName)?.sbLevel

    fun getCachedGoldCollection(playerName: String): String? =
        getValidCache(playerName)?.goldCollection

    fun getCachedSkillLevel(playerName: String, skillName: String): Double? {
        val data = getValidCache(playerName) ?: return null
        return data.skillLevel[skillName]
    }

    fun getCachedPets(playerName: String): List<Pet>? =
        getValidCache(playerName)?.pets

    fun getCachedPet(playerName: String, petName: String): Pet? {
        val data = getValidCache(playerName) ?: return null
        return data.pets.find { it.name.equals(petName, ignoreCase = true) }
    }

    fun getCachedInventories(playerName: String): Inventories? =
        getValidCache(playerName)?.inventories

    fun hasValidCache(playerName: String): Boolean =
        getValidCache(playerName) != null

    fun isFetching(playerName: String): Boolean =
        fetching.contains(playerName)

    fun fetchAsync(playerName: String, onComplete: (() -> Unit)? = null) {
        if (fetching.contains(playerName)) return
        fetching.add(playerName)

        thread(name = "Nyahirun-API-$playerName") {
            try {
                val uuid = getUUID(playerName) ?: return@thread

                val url = URL("https://worker.nyahirunaddons.workers.dev/?uuid=$uuid")
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

                    val selectedClass = root.getAsJsonObject("Class")
                        .get("SelectClass").asString

                    val classLV = mutableMapOf<String, Double>()

                    val classJson = root
                        .getAsJsonObject("Class")
                        .getAsJsonObject("Classes")

                    for ((key, value) in classJson.entrySet()) {
                        val obj = value.asJsonObject
                        classLV["class_$key"] = obj.get("Level").asString.toDouble()
                    }

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
                        extractPersonalBest(value, "plus")?.let {
                            floorPB["floor_$key"] = it
                        }
                    }

                    val masterJson = root
                        .getAsJsonObject("MasterFloor")
                        .getAsJsonObject("PersonalBest")

                    for ((key, value) in masterJson.entrySet()) {
                        extractPersonalBest(value, "masterplus")?.let {
                            masterPB["floor_$key"] = it
                        }
                    }

                    val magicalPower = root.get("MagicalPower").asInt

                    val sbLevel = root.get("SBLevel").asString

                    val goldCollection = root.get("GoldCollection").asString

                    val skillLV = mutableMapOf<String, Double>()

                    val skillJson = root
                        .getAsJsonObject("Skill")

                    for ((key, value) in skillJson.entrySet()) {
                        skillLV["skill_$key"] = value.asDouble
                    }

                    val pets = mutableListOf<Pet>()
                    val petsJson = root.getAsJsonArray("Pets")
                    for (petElement in petsJson) {
                        val obj = petElement.asJsonObject
                        val name = obj.get("name").asString
                        val item = obj.get("item").asString
                        val rarity = obj.get("rarity").asString
                        val level = obj.get("level").asInt
                        pets.add(Pet(name, item, rarity, level))
                    }

                    val invJson = resolveInventoriesObject(root.get("Inventories"))
                    val inventory = safeAsString(invJson?.get("inv_contents"))
                    val enderchest = safeAsString(invJson?.get("ender_chest_contents"))
                    val backpacks = parseBackpackContents(invJson?.get("backpack_contents"))
                    val armor = safeAsString(invJson?.get("inv_armor"))
                    val wardrobe = safeAsString(invJson?.get("wardrobe_contents"))
                    val wardrobeEquipped = safeAsInt(invJson?.get("wardrobe_equipped_slot"))
                    val equipment = safeAsString(invJson?.get("equipment_contents"))
                    val inventories = Inventories(
                        inventory,
                        enderchest,
                        backpacks,
                        armor,
                        wardrobe,
                        wardrobeEquipped,
                        equipment
                    )

                    val playerData = PlayerData(
                        cataLevel,
                        selectedClass,
                        classLV,
                        totalSecrets,
                        secretAverage,
                        floorPB,
                        masterPB,
                        magicalPower,
                        sbLevel,
                        goldCollection,
                        skillLV,
                        pets,
                        inventories
                    )

                    cache[playerName] = CachedPlayer(
                        playerData,
                        System.currentTimeMillis()
                    )
                }

                onComplete?.invoke()

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

    private fun extractPersonalBest(value: JsonElement, preferredKey: String): String? {
        return when {
            value.isJsonNull -> null
            value.isJsonPrimitive -> value.asString
            value.isJsonObject -> {
                val obj = value.asJsonObject
                val preferred = obj.get(preferredKey)
                if (preferred != null && !preferred.isJsonNull) {
                    preferred.asString
                } else {
                    obj.entrySet().firstOrNull { !it.value.isJsonNull }?.value?.asString
                }
            }
            else -> null
        }
    }

    private fun parseBackpackContents(backpackElement: JsonElement?): Map<String, String> {
        if (backpackElement == null || backpackElement.isJsonNull) return emptyMap()

        val result = mutableMapOf<String, String>()
        when {
            backpackElement.isJsonObject -> {
                for ((key, value) in backpackElement.asJsonObject.entrySet()) {
                    jsonElementToString(value)?.let { result[key] = it }
                }
            }

            backpackElement.isJsonArray -> {
                backpackElement.asJsonArray.forEachIndexed { index, element ->
                    jsonElementToString(element)?.let { result[index.toString()] = it }
                }
            }
        }
        return result
    }

    private fun resolveInventoriesObject(inventoriesElement: JsonElement?) = when {
        inventoriesElement == null || inventoriesElement.isJsonNull -> null
        inventoriesElement.isJsonObject -> inventoriesElement.asJsonObject
        inventoriesElement.isJsonArray -> inventoriesElement.asJsonArray
            .firstOrNull { it.isJsonObject }
            ?.asJsonObject
        else -> null
    }

    private fun safeAsString(element: JsonElement?): String? = when {
        element == null || element.isJsonNull -> null
        element.isJsonPrimitive -> element.asString
        else -> element.toString()
    }

    private fun safeAsInt(element: JsonElement?): Int? = when {
        element == null || element.isJsonNull -> null
        !element.isJsonPrimitive -> null
        else -> element.asString.toIntOrNull()
    }

    private fun jsonElementToString(element: JsonElement): String? {
        return when {
            element.isJsonNull -> null
            element.isJsonPrimitive -> element.asString
            else -> element.toString()
        }
    }
}
