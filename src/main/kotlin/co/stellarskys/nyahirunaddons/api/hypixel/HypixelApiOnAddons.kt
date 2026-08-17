package co.stellarskys.nyahirunaddons.api.hypixel

import co.stellarskys.nyahirunaddons.NyahirunAddons
import co.stellarskys.stella.api.handlers.Quasar
import co.stellarskys.stella.api.hypixel.HypixelApi
import co.stellarskys.stella.api.hypixel.SkyblockResponse

object HypixelApiOnAddons {
    fun fetchSkyblockProfile(
        uuid: String,
        cacheMs: Long = 300_000L,
        force: Boolean = false,
        onResult: (SkyblockResponse.SkyblockMember?) -> Unit
    ) {
        if (!force) {
            HypixelApi.ProfileCache.get(uuid, cacheMs)?.let {
                onResult(it)
                return
            }
        }

        val apiUrl = "${NyahirunAddons.API}/skyblock/profiles?uuid=$uuid"
        val url = if (force) "$apiUrl&t=${System.currentTimeMillis()}" else apiUrl

        Quasar.fetch<SkyblockResponse>(url) { result ->
            result.onSuccess { response ->
                val member = response.getActiveMember(uuid)
                if (member != null) {
                    HypixelApi.ProfileCache.put(uuid, member)
                }
                onResult(member)
            }.onFailure {
                onResult(null)
            }
        }
    }
}