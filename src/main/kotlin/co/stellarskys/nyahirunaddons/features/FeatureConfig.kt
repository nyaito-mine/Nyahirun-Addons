package co.stellarskys.nyahirunaddons.features

import co.stellarskys.stella.api.config.core.Config
import co.stellarskys.stella.api.config.core.ConfigSubcategory

object FeatureConfig {
    internal lateinit var sharedSections: Sections
    internal lateinit var addonSections: Sections

    data class Sections(
        val nonCategory: ConfigSubcategory,
        val disableUse: ConfigSubcategory,
        val chatHider: ConfigSubcategory,
        val partyFinder: ConfigSubcategory,
        val autoRefill: ConfigSubcategory,
        val renderHighlight: ConfigSubcategory,
        val notification: ConfigSubcategory,
        val terminalSolver: ConfigSubcategory,
        val terminalColor: ConfigSubcategory
    )

    private val subcategories = listOf(
        SubcategoryDef(
            name = "NonCategory", category = "-NA General-", features = listOf(
                ToggleDef(name = "CommandKeys"),
                ToggleDef(id = "EnableCooldown", name = "[CK] EnableCD", default = true, show = { settings -> settings["NonCategory.CommandKeys"] as Boolean }),
                ToggleDef(id = "CooldownMessage", name = "[CK] CDMessage", default = false, show = { settings -> settings["NonCategory.CommandKeys"] as Boolean })
            )
        ),
        SubcategoryDef(
            id = "DisableUse", name = "Disable Use", category = "-NA General-", features = listOf(
                ToggleDef(name = "Second SoulSand"),
                ToggleDef(name = "Place Tuba"),
                ToggleDef(name = "Place BOL"),
                ToggleDef(name = "Place Sceptre"),
                ToggleDef(name = "Place Head"),
                ToggleDef(name = "SBMenu")
            )
        ),
        SubcategoryDef(
            id = "ChatHider", name = "Chat Hider", category = "-NA General-", features = listOf(
                ToggleDef(name = "Obtained"),
                ToggleDef(name = "Milestone"),
                ToggleDef(name = "KillCombo"),
                ToggleDef(name = "Boss"),
                ToggleDef(name = "NPCMort"),
                ToggleDef(name = "TeleportCooldown"),
                ToggleDef(name = "Implosion"),
                ToggleDef(name = "TrapRoom"),
                ToggleDef(name = "Lever"),
                ToggleDef(name = "Chest"),
                ToggleDef(name = "IcePath"),
                ToggleDef(name = "MysticalForce"),
                ToggleDef(name = "LostAdventure"),
                ToggleDef(name = "Essence"),
                ToggleDef(name = "Blessing")
            )
        ),
        SubcategoryDef(id = "PartyFinder", name = "Party Finder", category = "-NA Dungeon-"),
        SubcategoryDef(id = "AutoRefill", name = "Auto Refill", category = "-NA Dungeon-"),
        SubcategoryDef(id = "RenderHighlight", name = "Render Highlight", category = "-NA Dungeon-"),
        SubcategoryDef(id = "Notification", name = "Notification", category = "-NA Dungeon-"),
        SubcategoryDef(id = "TerminalSolver", name = "Terminal Solver", category = "-NA Dungeon-"),
        SubcategoryDef(id = "TerminalColor", name = "Terminal Color", category = "-NA Dungeon-"),
    )

    fun register(target: Config): Sections {
        val created = mutableMapOf<String, ConfigSubcategory>()

        subcategories.forEach { spec ->
            var subcategoryId = spec.id ?: ""
            val subcategory = target.subcategory(spec.name, spec.category, subcategoryId, spec.description)
            subcategoryId = subcategoryId.ifEmpty { spec.name.toConfigKey() }
            spec.features.forEach { feature ->
                addFeature(subcategory, subcategoryId, feature)
            }
            created[spec.name] = subcategory
        }

        return Sections(
            nonCategory = created.getValue("NonCategory"),
            disableUse = created.getValue("Disable Use"),
            chatHider = created.getValue("Chat Hider"),
            partyFinder = created.getValue("Party Finder"),
            autoRefill = created.getValue("Auto Refill"),
            renderHighlight = created.getValue("Render Highlight"),
            notification = created.getValue("Notification"),
            terminalSolver = created.getValue("Terminal Solver"),
            terminalColor = created.getValue("Terminal Color")
        )
    }

    fun bindSharedSections(sections: Sections) {
        sharedSections = sections
        syncAddonToShared()
    }

    fun bindAddonSections(sections: Sections) {
        addonSections = sections
        syncAddonToShared()
    }

    fun syncAddonToShared() {
        if (!this::sharedSections.isInitialized) return
        if (!this::addonSections.isInitialized) return

        copySectionValues(addonSections, sharedSections)
    }

    private fun copySectionValues(source: Sections, target: Sections) {
        val fields = Sections::class.java.declaredFields
        for (field in fields) {
            if (!ConfigSubcategory::class.java.isAssignableFrom(field.type)) continue
            field.isAccessible = true

            val sourceSection = field.get(source) as? ConfigSubcategory ?: continue
            val targetSection = field.get(target) as? ConfigSubcategory ?: continue

            if (sourceSection.configName.isNotBlank()) {
                targetSection.value = sourceSection.value
            }

            for ((key, sourceElement) in sourceSection.elements) {
                val targetElement = targetSection.elements[key] ?: continue
                if (sourceElement.value != null) {
                    targetElement.value = sourceElement.value
                }
            }
        }
    }

    private fun addFeature(subcategory: ConfigSubcategory, subcategoryId: String, feature: FeatureDef) {
        when (feature) {
            is ToggleDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.toggle(
                    "$subcategoryId.$key",
                    feature.name,
                    feature.description,
                    feature.default,
                    feature.show
                )
            }

            is SliderDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.slider(
                    "$subcategoryId.$key",
                    feature.name,
                    feature.description,
                    feature.min,
                    feature.max,
                    feature.default,
                    feature.show
                )
            }

            is StepSliderDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.stepslider(
                    "$subcategoryId.$key",
                    feature.name,
                    feature.description,
                    feature.min,
                    feature.max,
                    feature.step,
                    feature.default,
                    feature.show
                )
            }

            is DropDownDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.dropdown(
                    "$subcategoryId.$key",
                    feature.name,
                    feature.description,
                    feature.options,
                    feature.default,
                    feature.show
                )
            }

            is ColorPickerDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.colorpicker(
                    "$subcategoryId.$key",
                    feature.name,
                    feature.description,
                    feature.default,
                    feature.show
                )
            }

            is TextInputDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.textinput(
                    "$subcategoryId.$key",
                    feature.name,
                    feature.description,
                    feature.default,
                    feature.show,
                    feature.onChange
                )
            }

            is KeyBindDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.keybind(
                    "$subcategoryId.$key",
                    feature.name,
                    feature.description,
                    feature.default,
                    feature.show
                )
            }

            is ButtonDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.button {
                    configName = "$subcategoryId.$key"
                    name = feature.name
                    description = feature.description
                    placeholder = feature.placeholder
                    onClick = feature.onClick
                }
            }

            is TextParagraphDef -> {
                val key = feature.id ?: feature.name.toConfigKey()
                subcategory.textparagraph {
                    configName = "$subcategoryId.$key"
                    name = feature.name
                    description = feature.text.ifBlank { feature.description }
                }
            }
        }
    }

    private fun String.toConfigKey(): String {
        return split(Regex("[^A-Za-z0-9]+"))
            .filter { it.isNotBlank() }
            .joinToString("") { part -> part.replaceFirstChar { ch -> ch.uppercaseChar() } }
    }
}

object SubCategories {
    val nonCategory get() = FeatureConfig.sharedSections.nonCategory
    val disableUse get() = FeatureConfig.sharedSections.disableUse
    val chatHider get() = FeatureConfig.sharedSections.chatHider
    val partyFinder get() = FeatureConfig.sharedSections.partyFinder
    val autoRefill get() = FeatureConfig.sharedSections.autoRefill
    val renderHighlight get() = FeatureConfig.sharedSections.renderHighlight
    val notification get() = FeatureConfig.sharedSections.notification
    val terminalSolver get() = FeatureConfig.sharedSections.terminalSolver
    val terminalColor get() = FeatureConfig.sharedSections.terminalColor
}

object NonCategory {
    private fun enabled(key: String): Boolean = FeatureConfig.sharedSections.nonCategory.elements[key]?.value as Boolean

    val commandKeys get() = enabled("NonCategory.CommandKeys")
    val enabledCooldown get() = enabled("NonCategory.EnableCooldown")
    val cooldownMessage get() = enabled("NonCategory.CooldownMessage")
}

object DisableUse {
    private fun enabled(key: String): Boolean = FeatureConfig.sharedSections.disableUse.elements[key]?.value as Boolean

    val secondSoulSand get() = enabled("DisableUse.SecondSoulSand")
    val placeTuba get() = enabled("DisableUse.PlaceTuba")
    val placeBOL get() = enabled("DisableUse.PlaceBOL")
    val placeSceptre get() = enabled("DisableUse.PlaceSceptre")
    val placeHead get() = enabled("DisableUse.PlaceHead")
    val sbMenu get() = enabled("DisableUse.SBMenu")
}

object ChatHider {
    private fun enabled(key: String): Boolean = FeatureConfig.sharedSections.chatHider.elements[key]?.value as Boolean

    val obtained get() = enabled("ChatHider.Obtained")
    val milestone get() = enabled("ChatHider.Milestone")
    val killCombo get() = enabled("ChatHider.KillCombo")
    val boss get() = enabled("ChatHider.Boss")
    val npcMort get() = enabled("ChatHider.NPCMort")
    val teleportCooldown get() = enabled("ChatHider.TeleportCooldown")
    val implosion get() = enabled("ChatHider.Implosion")
    val trapRoom get() = enabled("ChatHider.TrapRoom")
    val lever get() = enabled("ChatHider.Lever")
    val chest get() = enabled("ChatHider.Chest")
    val icePath get() = enabled("ChatHider.IcePath")
    val mysticalForce get() = enabled("ChatHider.MysticalForce")
    val lostAdventure get() = enabled("ChatHider.LostAdventure")
    val essence get() = enabled("ChatHider.Essence")
    val blessing get() = enabled("ChatHider.Blessing")
}
