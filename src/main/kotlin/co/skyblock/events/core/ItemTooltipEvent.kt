package co.skyblock.events.core

import co.stellarskys.stella.events.api.Event
import net.minecraft.network.chat.Component

sealed class ItemTooltipEvent {
    class Line(
        val lines: MutableList<Component>
    ) : Event()
}
