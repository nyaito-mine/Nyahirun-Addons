package co.skyblock.utils.dungeon.api

import com.google.gson.JsonParser
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object PlayerUtils {

    fun extractPlayerName(line: String): String {
        return line.split(":")[0].trim()
        // "MidNyaitoDye: Healer (43)" → "MidNyaitoDye"
    }

    fun getUUIDFromPlayerName(playerName: String): String? {
        return try {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$playerName")
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0") // 403対策
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode != 200) {
                null
            } else {
                InputStreamReader(conn.inputStream).use { reader ->
                    val json = JsonParser.parseReader(reader).asJsonObject
                    json.get("id").asString
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
