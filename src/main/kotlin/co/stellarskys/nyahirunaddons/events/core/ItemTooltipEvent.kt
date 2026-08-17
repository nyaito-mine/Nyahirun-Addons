package co.stellarskys.nyahirunaddons.events.core

import co.stellarskys.stella.api.events.Event
import net.minecraft.network.chat.Component

sealed class ItemTooltipEvent {
    class Lines(
        val lines: MutableList<Component>,
    ) : Event()
}