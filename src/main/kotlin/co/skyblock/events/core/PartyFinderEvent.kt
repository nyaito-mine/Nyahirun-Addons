package co.skyblock.events.core

import co.stellarskys.stella.events.api.Event
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import java.util.UUID
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket.PartyMember

sealed class PartyFinderEvent {
    class Queue(
        val floor: DungeonFloor,
        val note: String,
        val cataReq: Int,
        val classReq: Int
    ) : Event()

    class Leave : Event()

    class PartyInfo(
        val inParty: Boolean,
        val members: Map<UUID, PartyMember>
    ) : Event()
}