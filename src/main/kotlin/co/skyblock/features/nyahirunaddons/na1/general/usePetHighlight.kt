package co.skyblock.features.nyahirunaddons.na1.general

import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import java.awt.Color

@Module
object usePetHighlight : Feature("usePetHighlight") {

    val usePetHighlightConfig by config.property<Boolean>("usePetHighlight")
    val usePetHighlightColorConfig by config.property<Color>("usePetHighlightColor")

    override fun initialize() {
    }
}
