package co.stellarskys.nyahirunaddons.features.dungeon.notification

import co.stellarskys.stella.api.zenith.Aperture
import co.stellarskys.stella.api.zenith.Zenith
import net.minecraft.client.gui.GuiGraphicsExtractor

class NotificationScreen : Aperture("Notification") {
    private val page = Notification()

    override fun onRender(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, tickDelta: Float) {
        page.render(context, mouseX.toFloat(), mouseY.toFloat(), tickDelta)
    }

    override fun onMouseClick(button: Int, x: Double, y: Double, modifiers: Int) =
        page.mouseClicked(x.toFloat(), y.toFloat(), button)

    override fun onMouseRelease(button: Int, x: Double, y: Double, modifiers: Int): Boolean {
        page.mouseReleased(x.toFloat(), y.toFloat(), button)
        return false
    }

    override fun onMouseScroll(x: Double, y: Double, amount: Double, horizontalAmount: Double) =
        page.mouseScrolled(x.toFloat(), y.toFloat(), amount.toFloat(), horizontalAmount.toFloat())

    override fun onKeyPress(key: Int, scanCode: Int, modifiers: Int) =
        page.keyPressed(key, modifiers)

    override fun onCharTyped(char: Char) =
        page.charTyped(char)

    override fun onScreenClose() =
        page.screenClose()

    companion object {
        fun open() = Zenith.client.setScreen(NotificationScreen())
    }
}