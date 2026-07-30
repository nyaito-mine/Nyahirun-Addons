package co.stellarskys.nyahirunaddons.api.render.screen

import co.stellarskys.stella.api.config.ui.Palette
import co.stellarskys.stella.api.horizon.mc.ParentElement
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.abs

object MoreRender2D : ParentElement() {
    private const val MAX_TEXT_LENGTH = 256

    data class TextInputInteraction(
        val mouseClickX: Int? = null,
        val mouseClickY: Int? = null,
        val mouseButton: Int? = null,
        val keyCode: Int? = null,
        val typedChar: Char? = null,
    )

    private data class InputKey(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    private val textMap = mutableMapOf<InputKey, String>()
    private val caretMap = mutableMapOf<InputKey, Int>()
    private var focusedInput: InputKey? = null

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
        valueType: Boolean,
        interaction: TextInputInteraction = TextInputInteraction()
    ) {
        val key = InputKey(x, y, width, height)
        processInteraction(key, valueType, interaction)

        val text = textMap.getOrPut(key) { baseString.take(MAX_TEXT_LENGTH) }
        val caretIndex = caretMap.getOrPut(key) { text.length }.coerceIn(0, text.length)
        val borderColor = if (focusedInput == key) selectedColorCorrect else selectedColorWrong
        val textStartX = x + (width - GuiUtils.getWidth(text)) / 2

        ren2d.drawRect(context, x, y, width, height, color)
        ren2d.drawHollowRect(context, x, y, width, height, 1, borderColor)
        ren2d.drawString(context, text, textStartX, y + (height - GuiUtils.getHeight()) / 2)

        if (focusedInput == key) {
            val caretX = (textStartX + GuiUtils.getWidth(text.take(caretIndex))).coerceAtMost(x + width - 3)
            ren2d.drawRect(context, caretX, y + 4, 1, (height - 8).coerceAtLeast(1), Palette.Blue)
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

    private fun processInteraction(currentKey: InputKey, valueType: Boolean, interaction: TextInputInteraction) {
        if (interaction.mouseButton == 0 && interaction.mouseClickX != null && interaction.mouseClickY != null) {
            val clickedInside = interaction.mouseClickX in currentKey.x until (currentKey.x + currentKey.width) &&
                interaction.mouseClickY in currentKey.y until (currentKey.y + currentKey.height)
            if (clickedInside) {
                focusedInput = currentKey
                val text = textMap[currentKey].orEmpty()
                val textStartX = currentKey.x + (currentKey.width - GuiUtils.getWidth(text)) / 2
                caretMap[currentKey] = findCaretIndex(text, interaction.mouseClickX, textStartX)
            } else if (focusedInput == currentKey) {
                focusedInput = null
            }
        }

        if (focusedInput != currentKey) return

        interaction.keyCode?.let { keyCode ->
            handleKeyPressed(keyCode)
        }

        interaction.typedChar?.let { typedChar ->
            handleCharTyped(typedChar, valueType)
        }
    }

    private fun handleKeyPressed(keyCode: Int) {
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

            GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_ESCAPE -> {
                focusedInput = null
            }
        }
    }

    private fun handleCharTyped(char: Char, valueType: Boolean) {
        val focused = focusedInput ?: return
        if (char.isISOControl()) return
        if (valueType && char !in '0'..'9') return

        val current = textMap[focused] ?: ""
        val caret = caretMap[focused]?.coerceIn(0, current.length) ?: current.length
        if (current.length >= MAX_TEXT_LENGTH) return

        val updated = current.substring(0, caret) + char + current.substring(caret)
        textMap[focused] = updated
        caretMap[focused] = caret + 1
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
}
