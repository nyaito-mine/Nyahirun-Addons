package co.stellarskys.nyahirunaddons.features.general.nonCategory.commandKeys

import co.stellarskys.nyahirunaddons.api.GuiUtils
import co.stellarskys.nyahirunaddons.api.MoreRender2D
import co.stellarskys.stella.api.config.ui.Palette
import co.stellarskys.stella.api.config.ui.Palette.withAlpha
import co.stellarskys.stella.api.handlers.Signal.fakeMessage
import co.stellarskys.stella.api.horizon.animation.AnimType
import co.stellarskys.stella.utils.Utils
import net.minecraft.client.gui.GuiGraphicsExtractor
import tech.thatgravyboat.skyblockapi.utils.extentions.translated
import java.util.UUID

class Main : Page() {
    fun pageUpdate() {
        commandConfig = ConfigResponse.getCommands()
        if (selectedCommand !in commandConfig) {
            selectedCommand = commandConfig.firstOrNull()
        }
    }
    var commandConfig = ConfigResponse.getCommands()

    private var scrollOffset by Utils.animate<Float>(0.2, AnimType.EASE_OUT)

    companion object {
        const val WIDTH_SIZE = 172
        const val HEIGHT_SIZE = 26
        const val STEP_SIZE = 28
        const val BUTTON_SIZE = 84
        const val BUTTON_SPACE = 7
        const val PREVIEW_WIDTH = 130
        const val KEY_WIDTH = 116
        const val PREVIEW_BUTTON_SIZE = 56
        const val PREVIEW_BUTTON_SPACE = 4
    }

    private val totalHeight get() = commandConfig.size * STEP_SIZE + 10
    private var selectedCommand = commandConfig.firstOrNull()
    private var focusKeyBind = false
    private var targetOffset = 0f
    private var textInputInteraction = MoreRender2D.TextInputInteraction()
    private var lastSelectedCommandValue = 0
    private var lastSelectedCommandId: String? = selectedCommand?.id
    private var unsavedCommandKeyBind: String? = null
    private var unsavedCommandEnabled: Boolean? = null

    override fun onRender(context: GuiGraphicsExtractor, mouseX: Float, mouseY: Float, delta: Float) {
        // Config
        ren2d.drawHollowRect(context, 10, 25, 190, 215, 1, Palette.Sky)
        renderCommandConfig(context, 10, 30, mouseX, mouseY)
        ren2d.drawScrollbar(context, 193, 30, 175, scrollOffset, totalHeight, Palette.Sky)
        ren2d.drawRect(context, 10, 208, 190, 1, Palette.Sky)

        // OverView
        ren2d.drawHollowRect(context, 210, 25, PREVIEW_WIDTH, 215, 1, Palette.Sky)
        renderCommandConfigOverview(context, 210, 25, mouseX, mouseY)

        // Add / Delete Button
        context.translated(18, 212) {
            ren2d.drawRect(context, 0, 0, BUTTON_SIZE, HEIGHT_SIZE - 2, Palette.Sapphire.withAlpha(100))
            ren2d.drawHollowRect(context, 0, 0, BUTTON_SIZE, HEIGHT_SIZE - 2, 1, Palette.Blue)
            ren2d.drawRect(context, 0 + BUTTON_SIZE + BUTTON_SPACE, 0, BUTTON_SIZE, HEIGHT_SIZE - 2, Palette.Sapphire.withAlpha(100))
            ren2d.drawHollowRect(context, 0 + BUTTON_SIZE + BUTTON_SPACE, 0, BUTTON_SIZE, HEIGHT_SIZE - 2, 1, Palette.Blue)
            val strings = listOf("Add", "Delete")
            val scale = 1.05f
            ren2d.drawString(context, strings[0], (((BUTTON_SIZE - GuiUtils.getWidth(strings[0]) * scale) / 2) / scale).toInt(), (((HEIGHT_SIZE - GuiUtils.getHeight() * scale) / 2) / scale).toInt(), scale)
            ren2d.drawString(context, strings[1], ((BUTTON_SIZE + BUTTON_SPACE + (BUTTON_SIZE - GuiUtils.getWidth(strings[1]) * scale) / 2) / scale).toInt(), (((HEIGHT_SIZE - GuiUtils.getHeight() * scale) / 2) / scale).toInt(), scale)
        }
        textInputInteraction = MoreRender2D.TextInputInteraction()
    }

    private fun renderCommandConfig(context: GuiGraphicsExtractor, x: Int, y: Int, mouseX: Float, mouseY: Float) {
        ren2d.renderScrolled(context, x, y, 190, 175, scrollOffset) {
            renderCommandConfigGrid(context, 8, 0, x, y, commandConfig, mouseX, mouseY)
        }
    }

    private fun renderCommandConfigOverview(context: GuiGraphicsExtractor, x: Int, y: Int, mouseX: Float, mouseY: Float) {
        val command = selectedCommand ?: return

        val ax = x + (PREVIEW_WIDTH - KEY_WIDTH) / 2
        val ay = y + 45
        val by = ay + 30

        val explanationString = listOf("Command", "Cooldown Ticks")
        val explanationScale = 0.8f

        if (lastSelectedCommandId != command.id) {
            MoreRender2D.setTextInputValue(ax, ay, KEY_WIDTH, HEIGHT_SIZE, command.command)
            MoreRender2D.setTextInputValue(ax, (by + 38), KEY_WIDTH, HEIGHT_SIZE, command.cooldownTicks.toString())
            lastSelectedCommandId = command.id
        }
        // TextInput
        ren2d.drawString(context, explanationString[0], ((ax + 2) / explanationScale).toInt(), ((ay - 8) / explanationScale).toInt(), explanationScale)
        MoreRender2D.drawTextInput(context, ax, ay, KEY_WIDTH, HEIGHT_SIZE, Palette.Surface0.withAlpha(230), Palette.Blue, Palette.Sapphire.withAlpha(60), command.command, false, textInputInteraction)

        // KeyBind / Boolean Button
        context.translated(ax, by) {
            ren2d.drawRect(context, 0, 0, PREVIEW_BUTTON_SIZE, HEIGHT_SIZE - 2, Palette.Sapphire.withAlpha(100))
            ren2d.drawHollowRect(context, 0, 0, PREVIEW_BUTTON_SIZE, HEIGHT_SIZE - 2, 1, Palette.Blue)
            ren2d.drawRect(context, PREVIEW_BUTTON_SIZE + PREVIEW_BUTTON_SPACE, 0, PREVIEW_BUTTON_SIZE, HEIGHT_SIZE - 2, Palette.Sapphire.withAlpha(100))
            ren2d.drawHollowRect(context, PREVIEW_BUTTON_SIZE + PREVIEW_BUTTON_SPACE, 0, PREVIEW_BUTTON_SIZE, HEIGHT_SIZE - 2, 1, Palette.Blue)
            val keyBindType = unsavedCommandKeyBind ?: command.key
            val enabledType = unsavedCommandEnabled ?: command.enabled
            val strings = listOf(if (focusKeyBind) "_" else keyBindType, if (enabledType) "Enabled" else "Disabled")
            val scale = 1.05f
            ren2d.drawString(context, strings[0], (((PREVIEW_BUTTON_SIZE - GuiUtils.getWidth(strings[0]) * scale) / 2) / scale).toInt(), (((HEIGHT_SIZE - GuiUtils.getHeight() * scale) / 2) / scale).toInt(), scale)
            ren2d.drawString(context, strings[1], ((PREVIEW_BUTTON_SIZE + PREVIEW_BUTTON_SPACE + (PREVIEW_BUTTON_SIZE - GuiUtils.getWidth(strings[1]) * scale) / 2) / scale).toInt(), (((HEIGHT_SIZE - GuiUtils.getHeight() * scale) / 2) / scale).toInt(), scale)
        }

        // ValueInput
        ren2d.drawString(context, explanationString[1], ((ax + 2) / explanationScale).toInt(), ((by + 30) / explanationScale).toInt(), explanationScale)
        MoreRender2D.drawTextInput(context, ax, (by + 38), KEY_WIDTH, HEIGHT_SIZE, Palette.Surface0.withAlpha(230), Palette.Blue, Palette.Sapphire.withAlpha(60), command.cooldownTicks.toString(), true, textInputInteraction)

        // Done Button
        context.translated(ax, by + 80) {
            ren2d.drawRect(context, 0,0, KEY_WIDTH, HEIGHT_SIZE - 2, Palette.Sapphire.withAlpha(100))
            ren2d.drawHollowRect(context, 0, 0, KEY_WIDTH, HEIGHT_SIZE - 2, 1, Palette.Blue)
            val string = "Done"
            val scale = 1.05f
            ren2d.drawString(context, string, (((KEY_WIDTH - GuiUtils.getWidth(string) * scale) / 2) / scale).toInt(), (((HEIGHT_SIZE - GuiUtils.getHeight() * scale) / 2) / scale).toInt(), scale)
        }
    }

    private fun renderCommandConfigGrid(context: GuiGraphicsExtractor, sx: Int, sy: Int, ox: Int, oy: Int, config: List<ConfigResponse.Command>, mouseX: Float, mouseY: Float) {
        val inScissor = isAreaHovered(ox.toFloat(), oy.toFloat(), 190f, 175f, mouseX, mouseY)
        config.forEachIndexed { i, configs ->
            drawCommandConfig(context, sx, sy + i * STEP_SIZE, ox, oy, configs, mouseX, mouseY, inScissor)
        }
    }

    private fun drawCommandConfig(ctx: GuiGraphicsExtractor, ix: Int, iy: Int, ox: Int, oy: Int, config: ConfigResponse.Command, mouseX: Float, mouseY: Float, inScissor: Boolean) {
        ren2d.drawRect(ctx, ix, iy, WIDTH_SIZE, HEIGHT_SIZE, Palette.Sapphire.withAlpha(40))
        ren2d.drawHollowRect(ctx, ix, iy, WIDTH_SIZE, HEIGHT_SIZE, 1, if (config == selectedCommand) Palette.Blue else Palette.Sapphire.withAlpha(60))

        val scale = 1.05f
        ren2d.drawString(ctx, config.key, ix + 5, ((iy + 9) / scale).toInt(), scale)
        ren2d.drawString(ctx, config.command, ix + 65, ((iy + 9) / scale).toInt(), scale)
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
        focusKeyBind = false

        if (super.mouseClicked(mouseX, mouseY, button)) return true
        if (!isAreaHovered(18f, 30f, 337f, 206f, mouseX, mouseY)) return false

        val lx = (mouseX - absoluteX).toInt()
        val ly = (mouseY - absoluteY).toInt()
        val lys = (mouseY - absoluteY - 30 - scrollOffset).toInt()
        val row = Math.floorDiv(lys, STEP_SIZE)

        (lx < (18 + WIDTH_SIZE) && ly in 30 until 205 && lys.mod(STEP_SIZE) <= HEIGHT_SIZE)
            .let { insideSlot -> row.takeIf { insideSlot && it in commandConfig.indices } }
            ?.let {
                selectedCommand = commandConfig[it]
                lastSelectedCommandValue = it
                lastSelectedCommandId = null
                unsavedCommandKeyBind = null
                unsavedCommandEnabled = null
                return true
            }

        val ax = 210 + (PREVIEW_WIDTH - KEY_WIDTH) / 2
        val ay = 100

        val addHovered = lx in 18 until 18 + BUTTON_SIZE && ly >= 212
        val deleteHovered = lx in (18 + BUTTON_SIZE + BUTTON_SPACE) until (18 + BUTTON_SIZE * 2 + BUTTON_SPACE) && ly >= 212
        val keyBindHovered = lx in ax until (ax + PREVIEW_BUTTON_SIZE) && ly in ay until (ay + HEIGHT_SIZE - 2)
        val booleanHovered = lx in (ax + PREVIEW_BUTTON_SIZE + PREVIEW_BUTTON_SPACE) until (ax + PREVIEW_BUTTON_SIZE + PREVIEW_BUTTON_SPACE + PREVIEW_BUTTON_SIZE) && ly in ay until (ay + HEIGHT_SIZE - 2)
        val doneHovered = lx in ax until (ax + KEY_WIDTH) && ly in (ay + 80) until (ay + 80 + HEIGHT_SIZE - 2)

        return when {
            addHovered -> {
                ConfigResponse.addCommand(
                    ConfigResponse.Command(
                        id = UUID.randomUUID().toString(),
                        key = "None",
                        command = "Write Command",
                        enabled = false,
                        cooldownTicks = 4
                    )
                )
                pageUpdate()
                selectedCommand = commandConfig[commandConfig.lastIndex]
                lastSelectedCommandValue = commandConfig.lastIndex
                lastSelectedCommandId = null
                true
            }

            deleteHovered -> {
                if (selectedCommand != null) {
                    ConfigResponse.deleteCommand(selectedCommand!!.id)
                    pageUpdate()
                    commandConfig.lastIndex
                    if (commandConfig.lastIndex >= lastSelectedCommandValue) selectedCommand = commandConfig[lastSelectedCommandValue]
                    else if (commandConfig.isEmpty()) selectedCommand = null
                    else {
                        selectedCommand = commandConfig[lastSelectedCommandValue - 1]
                        lastSelectedCommandValue -= 1
                    }
                    lastSelectedCommandId = null
                    true
                } else {
                    fakeMessage("§b[Ny]§r No command selected to delete")
                    false
                }
            }

            keyBindHovered -> {
                focusKeyBind = true
                true
            }

            booleanHovered -> {
                var enabled = unsavedCommandEnabled ?: selectedCommand?.enabled ?: return false
                enabled = !enabled
                unsavedCommandEnabled = enabled
                true
            }

            doneHovered -> {
                ConfigResponse.updateCommand(
                    ConfigResponse.Command(
                        id = selectedCommand!!.id,
                        key = unsavedCommandKeyBind ?: selectedCommand!!.key,
                        command = MoreRender2D.getTextInputValue(ax, 70, KEY_WIDTH, HEIGHT_SIZE),
                        enabled = unsavedCommandEnabled ?: selectedCommand!!.enabled,
                        cooldownTicks = (MoreRender2D.getTextInputValue(ax, (ay + 38), KEY_WIDTH, HEIGHT_SIZE)).toInt()
                    )
                )
                unsavedCommandKeyBind = null
                unsavedCommandEnabled = null
                pageUpdate()
                selectedCommand = commandConfig[lastSelectedCommandValue]
                true
            }

            else -> false
        }
    }

    override fun keyPressed(keyCode: Int, modifiers: Int): Boolean {
        textInputInteraction = MoreRender2D.TextInputInteraction(keyCode = keyCode)
        if (focusKeyBind) {
            unsavedCommandKeyBind = GuiUtils.keyMap.entries.firstOrNull { it.value == keyCode }?.key
            focusKeyBind = false
        }
        return super.keyPressed(keyCode, modifiers)
    }

    override fun charTyped(char: Char): Boolean {
        textInputInteraction = MoreRender2D.TextInputInteraction(typedChar = char)
        return super.charTyped(char)
    }
}