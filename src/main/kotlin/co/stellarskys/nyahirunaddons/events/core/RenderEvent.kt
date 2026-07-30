package co.stellarskys.nyahirunaddons.events.core

import co.stellarskys.nyahirunaddons.api.render.world.RenderContext
import co.stellarskys.stella.api.events.Event

sealed class RenderEvent {
    class Draw(val context: RenderContext) : Event()
}
