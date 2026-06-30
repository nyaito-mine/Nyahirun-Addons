package co.stellarskys.nyahirunaddons.features

import co.stellarskys.stella.api.zenith.Zenith
import co.stellarskys.stella.api.config.core.Config
import java.awt.Color

sealed interface FeatureDef {
    val id: String?
}

data class ToggleDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val default: Boolean = false,
    val show: ((Config) -> Boolean)? = null
) : FeatureDef

data class SliderDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val min: Float,
    val max: Float,
    val default: Float,
    val show: ((Config) -> Boolean)? = null
) : FeatureDef

data class StepSliderDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val min: Int,
    val max: Int,
    val step: Int,
    val default: Int,
    val show: ((Config) -> Boolean)? = null
) : FeatureDef

data class DropDownDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val options: List<String>,
    val default: Int = 0,
    val show: ((Config) -> Boolean)? = null
) : FeatureDef

data class ColorPickerDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val default: Color = Color.WHITE,
    val show: ((Config) -> Boolean)? = null
) : FeatureDef

data class TextInputDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val default: String = "",
    val show: ((Config) -> Boolean)? = null,
    val onChange: ((String) -> Unit)? = null
) : FeatureDef

data class KeyBindDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val default: Int = Zenith.Keys.NONE,
    val show: ((Config) -> Boolean)? = null
) : FeatureDef

data class ButtonDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val placeholder: String = "Click",
    val onClick: (() -> Unit)? = null
) : FeatureDef

data class TextParagraphDef(
    override val id: String? = null,
    val name: String,
    val description: String = "",
    val text: String = ""
) : FeatureDef

data class SubcategoryDef(
    val id: String? = null,
    val name: String,
    val category: String,
    val description: String = "",
    val features: List<FeatureDef> = emptyList()
)
