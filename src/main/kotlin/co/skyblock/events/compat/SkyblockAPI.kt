package co.skyblock.events.compat

import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.party.DungeonPartyFinderQueueEvent
import tech.thatgravyboat.skyblockapi.api.events.party.PartyFinderLeaveQueueEvent
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.EventBus
import co.skyblock.events.core.PartyFinderEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.PartyInfoEvent

@Module
object SkyblockAPI {
    init {
        SkyBlockAPI.eventBus.register(this)
    }

    @Subscription
    fun onPartyFinderQueueUpdate(event: DungeonPartyFinderQueueEvent) {
        EventBus.post(PartyFinderEvent.Queue(event.floor, event.groupNote, event.dungeonLevelRequirement, event.classLevelRequirement))
    }

    @Subscription
    fun onPartyFinderLeaveUpdate(event: PartyFinderLeaveQueueEvent) {
        EventBus.post(PartyFinderEvent.Leave())
    }

    @Subscription
    fun onPartyInfoUpdate(event: PartyInfoEvent) {
        EventBus.post(PartyFinderEvent.PartyInfo(event.inParty, event.members))
    }
}