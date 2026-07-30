package co.stellarskys.nyahirunaddons.features.dungeon.notification

import co.stellarskys.stella.api.config.ui.Palette
import co.stellarskys.stella.api.config.ui.Palette.withAlpha
import co.stellarskys.stella.api.horizon.mc.ParentElement
import net.minecraft.client.gui.GuiGraphicsExtractor
import tech.thatgravyboat.skyblockapi.platform.pushPop

open class Page : ParentElement() {
    init {
        width = 350f
        height = 250f
        x = screenX
        y = screenY
    }

    protected val screenX get() = rez.scaledWidth / 2 - width / 2
    protected val screenY get() = rez.scaledHeight / 2 - height / 2

    open fun onRender(context: GuiGraphicsExtractor, mouseX: Float, mouseY: Float, delta: Float) {}

    override fun render(context: GuiGraphicsExtractor, mouseX: Float, mouseY: Float, delta: Float) {
        x = screenX
        y = screenY
        context.pushPop {
            context.pose().translate(x, y)
            ren2d.drawRect(context, 0, 0, width.toInt(), height.toInt(), Palette.Crust.withAlpha(150))
            ren2d.drawHollowRect(context, 0, 0, width.toInt(), height.toInt(), 1, Palette.Sky)
            ren2d.drawString(context, "Notification", 10, 10)
            onRender(context, mouseX, mouseY, delta)
        }
    }
}