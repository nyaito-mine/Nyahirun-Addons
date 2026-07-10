package co.stellarskys.nyahirunaddons.api

import co.stellarskys.stella.api.zenith.Zenith.Keys
import co.stellarskys.stella.api.zenith.Zenith.client

object GuiUtils {
    fun getWidth(string: String) = client.font.width(string)
    fun getHeight() = client.font.lineHeight

    val keyMap = mapOf(
        "A" to Keys.A, "B" to Keys.B, "C" to Keys.C, "D" to Keys.D, "E" to Keys.E,
        "F" to Keys.F, "G" to Keys.G, "H" to Keys.H, "I" to Keys.I, "J" to Keys.J,
        "K" to Keys.K, "L" to Keys.L, "M" to Keys.M, "N" to Keys.N, "O" to Keys.O,
        "P" to Keys.P, "Q" to Keys.Q, "R" to Keys.R, "S" to Keys.S, "T" to Keys.T,
        "U" to Keys.U, "V" to Keys.V, "W" to Keys.W, "X" to Keys.X, "Y" to Keys.Y,
        "Z" to Keys.Z,

        "0" to Keys.N_0, "1" to Keys.N_1, "2" to Keys.N_2, "3" to Keys.N_3, "4" to Keys.N_4,
        "5" to Keys.N_5, "6" to Keys.N_6, "7" to Keys.N_7, "8" to Keys.N_8, "9" to Keys.N_9,

        "F1" to Keys.F1, "F2" to Keys.F2, "F3" to Keys.F3, "F4" to Keys.F4, "F5" to Keys.F5,
        "F6" to Keys.F6, "F7" to Keys.F7, "F8" to Keys.F8, "F9" to Keys.F9, "F10" to Keys.F10,
        "F11" to Keys.F11, "F12" to Keys.F12,

        "Escape" to Keys.ESCAPE, "Enter" to Keys.ENTER, "Tab" to Keys.TAB, "BackSpace" to Keys.BACKSPACE, "Insert" to Keys.INSERT,
        "Delete" to Keys.DELETE, "Right" to Keys.RIGHT, "Left" to Keys.LEFT, "Down" to Keys.DOWN, "Up" to Keys.UP,
        "PageUp" to Keys.PAGE_UP, "PageDown" to Keys.PAGE_DOWN, "Home" to Keys.HOME, "End" to Keys.END, "CapsLock" to Keys.CAPS_LOCK,
        "ScrollLock" to Keys.SCROLL_LOCK, "NumLock" to Keys.NUM_LOCK, "PrintScreen" to Keys.PRINT_SCREEN, "Pause" to Keys.PAUSE,

        "KP0" to Keys.KP_0, "KP1" to Keys.KP_1, "KP2" to Keys.KP_2, "KP3" to Keys.KP_3, "KP4" to Keys.KP_4,
        "KP5" to Keys.KP_5, "KP6" to Keys.KP_6, "KP7" to Keys.KP_7, "KP8" to Keys.KP_8, "KP9" to Keys.KP_9,
        "KPDecimal" to Keys.KP_DECIMAL, "KPDivide" to Keys.KP_DIVIDE, "KPMultiply" to Keys.KP_MULTIPLY,
        "KPSubtract" to Keys.KP_SUBTRACT, "KPAdd" to Keys.KP_ADD,
        "KPEnter" to Keys.KP_ENTER, "KPEqual" to Keys.KP_EQUAL,

        "LShift" to Keys.L_SHIFT, "LControl" to Keys.L_CONTROL, "LAlt" to Keys.L_ALT, "LSuper" to Keys.L_SUPER,
        "RShift" to Keys.R_SHIFT, "RControl" to Keys.R_CONTROL, "RAlt" to Keys.R_ALT, "RSuper" to Keys.R_SUPER,
        "Menu" to Keys.MENU,

        "Space" to Keys.SPACE, "Apostrophe" to Keys.APOSTROPHE, "Comma" to Keys.COMMA, "Minus" to Keys.MINUS, "Period" to Keys.PERIOD,
        "Slash" to Keys.SLASH, "Semicolon" to Keys.SEMICOLON, "Equal" to Keys.EQUAL, "LBracket" to Keys.L_BRACKET, "Backslash" to Keys.BACKSLASH,
        "RBracket" to Keys.R_BRACKET, "GraveAccent" to Keys.GRAVE_ACCENT, "World1" to Keys.WORLD_1, "World2" to Keys.WORLD_2,

        "None" to Keys.NONE
    )
}