package co.skyblock.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import co.skyblock.utils.render.listeners.Event
import co.skyblock.utils.render.listeners.RenderListener
import java.util.ArrayList

object RenderEvent : Event<RenderListener> {

    lateinit var matrixStack: PoseStack
    var partialTicks: Float = 0.0f

    /**
     * Gets the RenderEvent instance.
     */
    fun get(matrixStack: PoseStack, partialTicks: Float): RenderEvent {
        this.matrixStack = matrixStack
        this.partialTicks = partialTicks
        return this
    }

    override fun fire(listeners: ArrayList<RenderListener>) {
        for (listener in listeners) {
            listener.onRender(matrixStack, partialTicks)
        }
    }

    override fun getEvent(): Class<RenderListener> {
        return RenderListener::class.java
    }
}
