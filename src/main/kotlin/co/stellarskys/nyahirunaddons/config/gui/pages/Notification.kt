package co.stellarskys.nyahirunaddons.config.gui.pages

import co.stellarskys.nyahirunaddons.NyahirunAddons
import co.stellarskys.nyahirunaddons.api.render.screen.GuiUtils
import co.stellarskys.nyahirunaddons.api.render.screen.MoreRender2D
import co.stellarskys.nyahirunaddons.config.gui.Page
import co.stellarskys.nyahirunaddons.config.gui.response.NotificationResponse
import co.stellarskys.stella.api.config.ui.Palette
import co.stellarskys.stella.api.config.ui.Palette.withAlpha
import co.stellarskys.stella.api.handlers.Signal.fakeMessage
import co.stellarskys.stella.api.horizon.animation.AnimType
import co.stellarskys.stella.api.zenith.client
import co.stellarskys.stella.utils.Utils
import net.minecraft.client.gui.GuiGraphicsExtractor
import tech.thatgravyboat.skyblockapi.utils.extentions.translated
import java.util.UUID
import kotlin.collections.contains

class Notification : Page("Notification") {
    fun pageUpdate() {
        notificationConfig = NotificationResponse.getCommands()
        if (selectedNotification !in notificationConfig) {
            selectedNotification = notificationConfig.firstOrNull()
        }
    }
    var notificationConfig = NotificationResponse.getCommands()

    private var scrollOffset by Utils.animate<Float>(0.2, AnimType.EASE_OUT)

    companion object {
        const val WIDTH_SIZE = 122
        const val HEIGHT_SIZE = 24
        const val STEP_SIZE = 28
        const val BUTTON_SIZE = 59
        const val BUTTON_SPACE = 5
        const val PREVIEW_WIDTH = 180
        const val KEY_WIDTH = 166
    }

    private val totalHeight get() = notificationConfig.size * STEP_SIZE + 10
    private var selectedNotification = notificationConfig.firstOrNull()
    private var targetOffset = 0f
    private var textInputInteraction = MoreRender2D.TextInputInteraction()
    private var lastSelectedNotificationValue = 0
    private var lastSelectedNotificationId: String? = null
    private var lastPressedKey: Int? = null
    private var unsavedNotificationEnabled: Boolean? = null

    override fun onRender(context: GuiGraphicsExtractor, mouseX: Float, mouseY: Float, delta: Float) {
        // Config
        ren2d.drawHollowRect(context, 10, 25, 140, 215, 1, Palette.Sky)
        renderNotificationConfig(context, 10, 30, mouseX, mouseY)
        ren2d.drawScrollbar(context, 143, 30, 175, scrollOffset, totalHeight, Palette.Sky)
        ren2d.drawRect(context, 10, 208, 140, 1, Palette.Sky)

        // OverView
        ren2d.drawHollowRect(context, 160, 25, PREVIEW_WIDTH, 215, 1, Palette.Sky)
        renderNotificationConfigOverview(context, 160, 25, mouseX, mouseY)

        // Add / Delete Button
        context.translated(18, 212) {
            ren2d.drawRect(context, 0, 0, BUTTON_SIZE, HEIGHT_SIZE, Palette.Sapphire.withAlpha(100))
            ren2d.drawHollowRect(context, 0, 0, BUTTON_SIZE, HEIGHT_SIZE, 1, Palette.Blue)
            ren2d.drawRect(context, 0 + BUTTON_SIZE + BUTTON_SPACE, 0, BUTTON_SIZE, HEIGHT_SIZE, Palette.Sapphire.withAlpha(100))
            ren2d.drawHollowRect(context, 0 + BUTTON_SIZE + BUTTON_SPACE, 0, BUTTON_SIZE, HEIGHT_SIZE, 1, Palette.Blue)
            val strings = listOf("Add", "Delete")
            val scale = 1.05f
            ren2d.drawString(context, strings[0], (((BUTTON_SIZE - GuiUtils.getWidth(strings[0]) * scale) / 2) / scale).toInt(), (((HEIGHT_SIZE - GuiUtils.getHeight() * scale) / 2) / scale).toInt(), scale)
            ren2d.drawString(context, strings[1], ((BUTTON_SIZE + BUTTON_SPACE + (BUTTON_SIZE - GuiUtils.getWidth(strings[1]) * scale) / 2) / scale).toInt(), (((HEIGHT_SIZE - GuiUtils.getHeight() * scale) / 2) / scale).toInt(), scale)
        }
        textInputInteraction = MoreRender2D.TextInputInteraction()
    }

    private fun renderNotificationConfig(context: GuiGraphicsExtractor, x: Int, y: Int, mouseX: Float, mouseY: Float) {
        ren2d.renderScrolled(context, x, y, 190, 175, scrollOffset) {
            renderNotificationConfigGrid(context, 8, 0, x, y, notificationConfig, mouseX, mouseY)
        }
    }

    private fun renderNotificationConfigOverview(context: GuiGraphicsExtractor, x: Int, y: Int, mouseX: Float, mouseY: Float) {
        val notification = selectedNotification ?: return

        val ax = x + (PREVIEW_WIDTH - KEY_WIDTH) / 2
        val ay = y + 15
        val by = (ay + 34 * 5 - 6)

        val explanationString = listOf("Notification (Color [^a, ^b...])", "Trigger", "Display Ticks", "Sound", "Volume (1.0)", "Pitch (0.5)")
        val explanationScale = 0.8f
        val scale = 1.05f

        if (lastSelectedNotificationId != notification.id) {
            MoreRender2D.setTextInputValue(ax, ay, KEY_WIDTH, HEIGHT_SIZE, notification.notification)
            MoreRender2D.setTextInputValue(ax, (ay + 34), KEY_WIDTH, HEIGHT_SIZE, notification.trigger)
            MoreRender2D.setTextInputValue(ax, (ay + 34 * 2), KEY_WIDTH, HEIGHT_SIZE, notification.displayTicks.toString())
            MoreRender2D.setTextInputValue(ax, (ay + 34 * 3), KEY_WIDTH, HEIGHT_SIZE, notification.sound)
            MoreRender2D.setTextInputValue(ax, (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE, notification.soundVolume.toString())
            MoreRender2D.setTextInputValue(ax + 2 + ((KEY_WIDTH / 2)), (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE, notification.soundPitch.toString())
            lastSelectedNotificationId = notification.id
        }
        // Notification / § Button
        ren2d.drawString(context, explanationString[0], ((ax + 2) / explanationScale).toInt(), ((ay - 8) / explanationScale).toInt(), explanationScale)
        MoreRender2D.drawTextInput(context, ax, ay, KEY_WIDTH, HEIGHT_SIZE, Palette.Surface0.withAlpha(230), Palette.Blue, Palette.Sapphire.withAlpha(60), notification.notification, textInputInteraction)

        // Trigger
        ren2d.drawString(context, explanationString[1], ((ax + 2) / explanationScale).toInt(), ((ay - 8 + 34) / explanationScale).toInt(), explanationScale)
        MoreRender2D.drawTextInput(context, ax, (ay + 34), KEY_WIDTH, HEIGHT_SIZE, Palette.Surface0.withAlpha(230), Palette.Blue, Palette.Sapphire.withAlpha(60), notification.trigger, textInputInteraction)

        // Display Ticks
        ren2d.drawString(context, explanationString[2], ((ax + 2) / explanationScale).toInt(), ((ay - 8 + 34 * 2) / explanationScale).toInt(), explanationScale)
        MoreRender2D.drawTextInput(context, ax, (ay + 34 * 2), KEY_WIDTH, HEIGHT_SIZE, Palette.Surface0.withAlpha(230), Palette.Blue, Palette.Sapphire.withAlpha(60), notification.displayTicks.toString(), textInputInteraction, "Int")

        //Sound
        ren2d.drawString(context, explanationString[3], ((ax + 2) / explanationScale).toInt(), ((ay - 8 + 34 * 3) / explanationScale).toInt(), explanationScale)
        MoreRender2D.drawTextInput(context, ax, (ay + 34 * 3), KEY_WIDTH, HEIGHT_SIZE, Palette.Surface0.withAlpha(230), Palette.Blue, Palette.Sapphire.withAlpha(60), notification.sound, textInputInteraction)

        //Sound Volume / Sound Pitch
        ren2d.drawString(context, explanationString[4], ((ax + 2) / explanationScale).toInt(), ((ay - 8 + 34 * 4) / explanationScale).toInt(), explanationScale)
        MoreRender2D.drawTextInput(context, ax, (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE, Palette.Surface0.withAlpha(230), Palette.Blue, Palette.Sapphire.withAlpha(60), notification.soundVolume.toString(), textInputInteraction, "Float")
        ren2d.drawString(context, explanationString[5], ((ax + 4 + (KEY_WIDTH / 2)) / explanationScale).toInt(), ((ay - 8 + 34 * 4) / explanationScale).toInt(), explanationScale)
        MoreRender2D.drawTextInput(context, ax + 2 + ((KEY_WIDTH / 2)), (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE, Palette.Surface0.withAlpha(230), Palette.Blue, Palette.Sapphire.withAlpha(60), notification.soundPitch.toString(), textInputInteraction, "Float")

        // Boolean Button
        context.translated(ax, by) {
            ren2d.drawRect(context, 0, 0, KEY_WIDTH, HEIGHT_SIZE, Palette.Sapphire.withAlpha(100))
            ren2d.drawHollowRect(context, 0, 0, KEY_WIDTH, HEIGHT_SIZE, 1, Palette.Blue)
            val enabledType = unsavedNotificationEnabled ?: notification.enabled
            val string = if (enabledType) "Enabled" else "Disabled"
            ren2d.drawString(context, string, (((KEY_WIDTH - GuiUtils.getWidth(string) * scale) / 2) / scale).toInt(), (((HEIGHT_SIZE - GuiUtils.getHeight() * scale) / 2) / scale).toInt(), scale)
        }
    }

    private fun renderNotificationConfigGrid(context: GuiGraphicsExtractor, sx: Int, sy: Int, ox: Int, oy: Int, config: List<NotificationResponse.Entry>, mouseX: Float, mouseY: Float) {
        val inScissor = isAreaHovered(ox.toFloat(), oy.toFloat(), 190f, 175f, mouseX, mouseY)
        config.forEachIndexed { i, configs ->
            drawNotificationConfig(context, sx, sy + i * STEP_SIZE, ox, oy, configs, mouseX, mouseY, inScissor)
        }
    }

    private fun drawNotificationConfig(ctx: GuiGraphicsExtractor, ix: Int, iy: Int, ox: Int, oy: Int, config: NotificationResponse.Entry, mouseX: Float, mouseY: Float, inScissor: Boolean) {
        ren2d.drawRect(ctx, ix, iy, WIDTH_SIZE, HEIGHT_SIZE + 2, Palette.Sapphire.withAlpha(40))
        ren2d.drawHollowRect(ctx, ix, iy, WIDTH_SIZE, HEIGHT_SIZE + 2, 1, if (config == selectedNotification) Palette.Blue else Palette.Sapphire.withAlpha(60))

        val displayText = if (config.notification.length > 15) {
            config.notification.take(15) + "..."
        } else {
            config.notification
        }
        val scale = 1.05f
        ren2d.drawString(ctx, displayText, ix + 5, ((iy + 9) / scale).toInt(), scale)
    }

    override fun mouseScrolled(mouseX: Float, mouseY: Float, amount: Float, horizontalAmount: Float): Boolean {
        if (isAreaHovered(10f, 25f, 200f, 185f, mouseX, mouseY)) {
            targetOffset = ren2d.calculateScroll(targetOffset, amount, totalHeight, 175)
            scrollOffset = targetOffset
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, amount, horizontalAmount)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        textInputInteraction = MoreRender2D.TextInputInteraction(
            mouseClickX = (mouseX - absoluteX).toInt(),
            mouseClickY = (mouseY - absoluteY).toInt(),
            mouseButton = button
        )

        if (super.mouseClicked(mouseX, mouseY, button)) return true
        if (!isAreaHovered(18f, 30f, 337f, 206f, mouseX, mouseY)) return false

        val lx = (mouseX - absoluteX).toInt()
        val ly = (mouseY - absoluteY).toInt()
        val lys = (mouseY - absoluteY - 30 - scrollOffset).toInt()
        val row = Math.floorDiv(lys, STEP_SIZE)

        val ax = 160 + (PREVIEW_WIDTH - KEY_WIDTH) / 2
        val ay = 40
        val by = (ay + 34 * 5 - 6)

        (lx < (18 + WIDTH_SIZE) && ly in 30 until 205 && lys.mod(STEP_SIZE) <= HEIGHT_SIZE + 2)
            .let { insideSlot -> row.takeIf { insideSlot && it in notificationConfig.indices } }
            ?.let {
                NotificationResponse.updateCommand(
                    NotificationResponse.Entry(
                        id = selectedNotification!!.id,
                        enabled = unsavedNotificationEnabled ?: selectedNotification!!.enabled,
                        notification = (MoreRender2D.getTextInputValue(ax, ay, KEY_WIDTH, HEIGHT_SIZE)),
                        trigger = (MoreRender2D.getTextInputValue(ax, (ay + 34), KEY_WIDTH, HEIGHT_SIZE)),
                        displayTicks = if (MoreRender2D.getTextInputValue(ax, (ay + 34 * 2), KEY_WIDTH, HEIGHT_SIZE) == "") 0 else (MoreRender2D.getTextInputValue(ax, (ay + 34 * 2), KEY_WIDTH, HEIGHT_SIZE)).toInt(),
                        sound = MoreRender2D.getTextInputValue(ax, (ay + 34 * 3), KEY_WIDTH, HEIGHT_SIZE),
                        soundVolume = MoreRender2D.getTextInputValue(ax, (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE).toFloat(),
                        soundPitch = MoreRender2D.getTextInputValue(ax + 2 + ((KEY_WIDTH / 2)), (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE).toFloat()
                    )
                )
                unsavedNotificationEnabled = null
                pageUpdate()
                selectedNotification = notificationConfig[it]
                lastSelectedNotificationValue = it
                lastSelectedNotificationId = null
                return true
            }

        val addHovered = lx in 18 until 18 + BUTTON_SIZE && ly >= 212
        val deleteHovered = lx in (18 + BUTTON_SIZE + BUTTON_SPACE) until (18 + BUTTON_SIZE * 2 + BUTTON_SPACE) && ly >= 212
        val booleanHovered = lx in ax until (ax + KEY_WIDTH) && ly in by until (by + HEIGHT_SIZE)

        return when {
            addHovered -> {
                if (selectedNotification != null) {
                    NotificationResponse.updateCommand(
                        NotificationResponse.Entry(
                            id = selectedNotification!!.id,
                            enabled = unsavedNotificationEnabled ?: selectedNotification!!.enabled,
                            notification = (MoreRender2D.getTextInputValue(ax, ay, KEY_WIDTH, HEIGHT_SIZE)),
                            trigger = (MoreRender2D.getTextInputValue(ax, (ay + 34), KEY_WIDTH, HEIGHT_SIZE)),
                            displayTicks = if (MoreRender2D.getTextInputValue(ax, (ay + 34 * 2), KEY_WIDTH, HEIGHT_SIZE) == "") 0 else (MoreRender2D.getTextInputValue(ax, (ay + 34 * 2), KEY_WIDTH, HEIGHT_SIZE)).toInt(),
                            sound = MoreRender2D.getTextInputValue(ax, (ay + 34 * 3), KEY_WIDTH, HEIGHT_SIZE),
                            soundVolume = MoreRender2D.getTextInputValue(ax, (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE).toFloat(),
                            soundPitch = MoreRender2D.getTextInputValue(ax + 2 + ((KEY_WIDTH / 2)), (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE).toFloat()
                        )
                    )
                }
                unsavedNotificationEnabled = null
                NotificationResponse.addCommand(
                    NotificationResponse.Entry(
                        id = UUID.randomUUID().toString(),
                        enabled = true,
                        notification = "^l^c",
                        trigger = "",
                        displayTicks = 30,
                        sound = "entity.experience_orb.pickup",
                        soundVolume = 1.0f,
                        soundPitch = 0.5f,
                    )
                )
                pageUpdate()
                selectedNotification = if (notificationConfig.lastIndex == -1) notificationConfig[0] else notificationConfig[notificationConfig.lastIndex]
                lastSelectedNotificationValue = notificationConfig.lastIndex
                lastSelectedNotificationId = null
                true
            }

            deleteHovered -> {
                if (selectedNotification != null) {
                    NotificationResponse.deleteCommand(selectedNotification!!.id)
                    unsavedNotificationEnabled = null
                    pageUpdate()
                    notificationConfig.lastIndex
                    if (notificationConfig.lastIndex >= lastSelectedNotificationValue) selectedNotification = notificationConfig[lastSelectedNotificationValue]
                    else if (notificationConfig.isEmpty()) selectedNotification = null
                    else {
                        selectedNotification = notificationConfig[lastSelectedNotificationValue - 1]
                        lastSelectedNotificationValue -= 1
                    }
                    lastSelectedNotificationId = null
                    true
                } else {
                    fakeMessage("${NyahirunAddons.SHORTPREFIX} No notification selected to delete")
                    false
                }
            }

            booleanHovered -> {
                var enabled = unsavedNotificationEnabled ?: selectedNotification?.enabled ?: return false
                enabled = !enabled
                unsavedNotificationEnabled = enabled
                true
            }

            else -> false
        }
    }

    override fun keyPressed(keyCode: Int, modifiers: Int): Boolean {
        textInputInteraction = MoreRender2D.TextInputInteraction(keyCode = keyCode, modifiers = modifiers)
        return super.keyPressed(keyCode, modifiers)
    }

    override fun charTyped(char: Char): Boolean {
        textInputInteraction = MoreRender2D.TextInputInteraction(typedChar = char)
        return super.charTyped(char)
    }

    override fun screenClose() {
        if (lastPressedKey == 256) {
            lastPressedKey = null
            return
        }
        val ax = 160 + (PREVIEW_WIDTH - KEY_WIDTH) / 2
        val ay = 40

        if (selectedNotification != null) {
            NotificationResponse.updateCommand(
                NotificationResponse.Entry(
                    id = selectedNotification!!.id,
                    enabled = unsavedNotificationEnabled ?: selectedNotification!!.enabled,
                    notification = (MoreRender2D.getTextInputValue(ax, ay, KEY_WIDTH, HEIGHT_SIZE)),
                    trigger = (MoreRender2D.getTextInputValue(ax, (ay + 34), KEY_WIDTH, HEIGHT_SIZE)),
                    displayTicks = if (MoreRender2D.getTextInputValue(ax, (ay + 34 * 2), KEY_WIDTH, HEIGHT_SIZE) == "") 0 else (MoreRender2D.getTextInputValue(ax, (ay + 34 * 2), KEY_WIDTH, HEIGHT_SIZE)).toInt(),
                    sound = MoreRender2D.getTextInputValue(ax, (ay + 34 * 3), KEY_WIDTH, HEIGHT_SIZE),
                    soundVolume = MoreRender2D.getTextInputValue(ax, (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE).toFloat(),
                    soundPitch = MoreRender2D.getTextInputValue(ax + 2 + ((KEY_WIDTH / 2)), (ay + 34 * 4), (KEY_WIDTH / 2) - 2, HEIGHT_SIZE).toFloat()
                )
            )
        }

        lastSelectedNotificationValue = 0
        lastSelectedNotificationId = null
        lastPressedKey = null
        unsavedNotificationEnabled = null
        client.setScreen(null)
    }
}