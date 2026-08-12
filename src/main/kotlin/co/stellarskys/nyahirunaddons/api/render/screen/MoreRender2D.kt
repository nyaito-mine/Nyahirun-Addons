package co.stellarskys.nyahirunaddons.api.render.screen

import co.stellarskys.stella.api.config.ui.Palette
import co.stellarskys.stella.api.horizon.mc.ParentElement
import co.stellarskys.stella.api.zenith.client
import co.stellarskys.stella.events.EventBus
import co.stellarskys.stella.events.core.TickEvent
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.abs

object MoreRender2D : ParentElement() {
    init {
        EventBus.on<TickEvent.Client> {
            if (focused) {
                time++
                if (time >= 20) time = 0
            } else {
                time = 0
            }
        }
    }

    private const val MAX_TEXT_LENGTH = 256

    data class TextInputInteraction(
        val mouseClickX: Int? = null,
        val mouseClickY: Int? = null,
        val mouseButton: Int? = null,
        val keepFocus: Boolean = false,
        val keyCode: Int? = null,
        val modifiers: Int? = null,
        val typedChar: Char? = null,
    )

    private data class InputKey(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    private data class TextRenderState(
        val visibleText: String,
        val visibleStart: Int,
        val textStartX: Int
    )

    private val textMap = mutableMapOf<InputKey, String>()
    private val caretMap = mutableMapOf<InputKey, Int>()
    private var focusedInput: InputKey? = null
    private var focused = false
    private var time = 0

    override fun render(context: GuiGraphicsExtractor, mouseX: Float, mouseY: Float, delta: Float) = Unit

    fun drawTextInput(
        context: GuiGraphicsExtractor,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Color,
        selectedColorCorrect: Color,
        selectedColorWrong: Color,
        baseString: String,
        interaction: TextInputInteraction = TextInputInteraction(),
        valueType: String = "String"
    ) {
        val key = InputKey(x, y, width, height)
        processInteraction(key, valueType, interaction)

        val text = textMap.getOrPut(key) { baseString.take(MAX_TEXT_LENGTH) }
        val caretIndex = caretMap.getOrPut(key) { text.length }.coerceIn(0, text.length)
        val borderColor = if (focusedInput == key) selectedColorCorrect else selectedColorWrong
        val renderState = buildTextRenderState(key, text, caretIndex)
        val caretVisibleIndex = (caretIndex - renderState.visibleStart).coerceIn(0, renderState.visibleText.length)

        ren2d.drawRect(context, x, y, width, height, color)
        ren2d.drawHollowRect(context, x, y, width, height, 1, borderColor)
        ren2d.drawString(context, renderState.visibleText, renderState.textStartX, y + (height - GuiUtils.getHeight()) / 2)

        if (focusedInput == key) {
            val caretX = (renderState.textStartX + GuiUtils.getWidth(renderState.visibleText.take(caretVisibleIndex))).coerceAtMost(x + width - 3)
            if (time <= 10) ren2d.drawRect(context, caretX, y + 4, 1, (height - 8).coerceAtLeast(1), Palette.Blue)
        }
    }

    fun getTextInputValue(x: Int, y: Int, width: Int, height: Int): String {
        val key = InputKey(x, y, width, height)
        return textMap[key].orEmpty()
    }

    fun setTextInputValue(x: Int, y: Int, width: Int, height: Int, value: String) {
        val key = InputKey(x, y, width, height)
        val normalized = value.take(MAX_TEXT_LENGTH)
        textMap[key] = normalized
        caretMap[key] = normalized.length
    }

    private fun processInteraction(currentKey: InputKey, valueType: String, interaction: TextInputInteraction) {
        if (interaction.mouseButton == 0 && interaction.mouseClickX != null && interaction.mouseClickY != null) {
            val clickedInside = interaction.mouseClickX in currentKey.x until (currentKey.x + currentKey.width) &&
                interaction.mouseClickY in currentKey.y until (currentKey.y + currentKey.height)
            if (clickedInside) {
                focused = true
                focusedInput = currentKey
                val text = textMap[currentKey].orEmpty()
                val currentCaret = caretMap[currentKey]?.coerceIn(0, text.length) ?: text.length
                val renderState = buildTextRenderState(currentKey, text, currentCaret)
                val visibleCaret = findCaretIndex(renderState.visibleText, interaction.mouseClickX, renderState.textStartX)
                caretMap[currentKey] = (renderState.visibleStart + visibleCaret).coerceIn(0, text.length)
            } else if (focusedInput == currentKey && !interaction.keepFocus) {
                focused = false
                focusedInput = null
            }
        }

        if (focusedInput != currentKey) return

        interaction.keyCode?.let { keyCode ->
            handleKeyPressed(keyCode, interaction.modifiers ?: 0, valueType)
        }

        interaction.typedChar?.let { typedChar ->
            handleCharTyped(typedChar, valueType)
        }
    }

    private fun handleKeyPressed(keyCode: Int, modifiers: Int, valueType: String) {
        val focused = focusedInput ?: return
        val current = textMap[focused] ?: return
        val caret = caretMap[focused]?.coerceIn(0, current.length) ?: current.length

        when (keyCode) {
            GLFW.GLFW_KEY_BACKSPACE -> {
                if (caret > 0) {
                    textMap[focused] = current.removeRange(caret - 1, caret)
                    caretMap[focused] = caret - 1
                }
            }

            GLFW.GLFW_KEY_LEFT -> {
                caretMap[focused] = (caret - 1).coerceAtLeast(0)
            }

            GLFW.GLFW_KEY_RIGHT -> {
                caretMap[focused] = (caret + 1).coerceAtMost(current.length)
            }

            GLFW.GLFW_KEY_V -> {
                if ((modifiers and GLFW.GLFW_MOD_CONTROL) != 0) {
                    pasteFromClipboard(focused, valueType)
                }
            }

            GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_ESCAPE -> {
                focusedInput = null
            }
        }
    }

    private fun pasteFromClipboard(focused: InputKey, valueType: String) {
        val clipboardText = getClipboardText()
        if (clipboardText.isEmpty()) return

        val current = textMap[focused] ?: ""

        val sanitized = if (valueType == "Int") {
            clipboardText.filter { it in '0'..'9' }
        } else if (valueType == "Float") {
            val filtered = clipboardText.filter { it in '0'..'9' || it == '.' }
            if ('.' in current && '.' in filtered) return
            if (filtered.count { it == '.' } > 1) return
            filtered
        } else {
            clipboardText.filterNot { it.isISOControl() }
        }
        if (sanitized.isEmpty()) return

        val remainingLength = MAX_TEXT_LENGTH - current.length
        if (remainingLength <= 0) return

        val inserted = sanitized.take(remainingLength)
        insertTextAtCaret(focused, inserted)
    }

    private fun getClipboardText(): String {
        return try {
            client.keyboardHandler.clipboard
        } catch (_: IllegalStateException) {
            ""
        }
    }

    private fun handleCharTyped(char: Char, valueType: String) {
        val focused = focusedInput ?: return
        if (char.isISOControl()) return
        if (valueType == "Int" && (char !in '0'..'9')) return

        val current = textMap[focused] ?: ""

        if (valueType == "Float") {
            if (char !in '0'..'9' && char != '.') return
            if (char == '.' && current.contains('.')) return
        }

        if (current.length >= MAX_TEXT_LENGTH) return

        insertTextAtCaret(focused, char.toString())
    }

    private fun findCaretIndex(text: String, mouseX: Int, textStartX: Int): Int {
        if (text.isEmpty()) return 0
        if (mouseX <= textStartX) return 0

        var bestIndex = text.length
        var bestDistance = Int.MAX_VALUE
        for (index in 0..text.length) {
            val candidateX = textStartX + GuiUtils.getWidth(text.take(index))
            val distance = abs(candidateX - mouseX)
            if (distance < bestDistance) {
                bestDistance = distance
                bestIndex = index
            }
        }
        return bestIndex
    }

    private fun buildTextRenderState(key: InputKey, text: String, caretIndex: Int): TextRenderState {
        val horizontalPadding = 3
        val availableWidth = (key.width - horizontalPadding * 2).coerceAtLeast(1)
        if (GuiUtils.getWidth(text) <= availableWidth) {
            return TextRenderState(
                visibleText = text,
                visibleStart = 0,
                textStartX = key.x + (key.width - GuiUtils.getWidth(text)) / 2
            )
        }

        val clampedCaret = caretIndex.coerceIn(0, text.length)
        var start = clampedCaret
        while (start > 0 && GuiUtils.getWidth(text.substring(start - 1, clampedCaret)) <= availableWidth) {
            start--
        }

        var end = clampedCaret
        while (end < text.length && GuiUtils.getWidth(text.substring(start, end + 1)) <= availableWidth) {
            end++
        }

        val visibleText = text.substring(start, end)
        return TextRenderState(
            visibleText = visibleText,
            visibleStart = start,
            textStartX = key.x + horizontalPadding
        )
    }

    private fun insertTextAtCaret(key: InputKey, inserted: String): Boolean {
        if (inserted.isEmpty()) return false
        val current = textMap[key] ?: ""
        val caret = caretMap[key]?.coerceIn(0, current.length) ?: current.length
        val remainingLength = MAX_TEXT_LENGTH - current.length
        if (remainingLength <= 0) return false

        val clipped = inserted.take(remainingLength)
        if (clipped.isEmpty()) return false

        val updated = current.substring(0, caret) + clipped + current.substring(caret)
        textMap[key] = updated
        caretMap[key] = caret + clipped.length
        return true
    }
}
