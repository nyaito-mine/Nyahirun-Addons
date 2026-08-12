package co.stellarskys.nyahirunaddons.features

import co.stellarskys.nyahirunaddons.config.gui.Screen
import co.stellarskys.stella.api.config.core.Config
import co.stellarskys.stella.api.config.core.ConfigSubcategory
import java.awt.Color

object FeatureConfig {
    internal lateinit var sharedSections: Sections
    internal lateinit var addonSections: Sections

    data class Sections(
        val stellaNavExtra: ConfigSubcategory,
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
        // StellaNav
        SubcategoryDef(name = "Extra", category = "StellaNav", features = listOf(
            ToggleDef(id = "DoorsFill", name = "-NA- Doors Fill"),
            ColorPickerDef(id = "DoorsFillKeyColor", name = "-NA- Fill Key Color", default = Color(0, 255, 0, 153), show = { settings -> settings["Extra.DoorsFill"] as Boolean }),
            ColorPickerDef(id = "DoorsFillNoKeyColor", name = "-NA- Fill No Key Color", default = Color(255, 0, 0, 153), show = { settings -> settings["Extra.DoorsFill"] as Boolean }),
            ToggleDef(id = "DoorsTracer", name = "-NA- Doors Tracer"),
            ColorPickerDef(id = "DoorsTracerKeyColor", name = "-NA- Tracer Key Color", default = Color(0, 255, 0, 255), show = { settings -> settings["Extra.DoorsTracer"] as Boolean }),
            ColorPickerDef(id = "DoorsTracerNoKeyColor", name = "-NA- Tracer No Key Color", default = Color(255, 0, 0, 255), show = { settings -> settings["Extra.DoorsTracer"] as Boolean })
            )
        ),
        //General
        SubcategoryDef(name = "NonCategory", category = "-NA General-", features = listOf(
            ToggleDef(name = "CommandKeys"),
            ToggleDef(id = "EnableCooldown", name = "[CK] EnableCD", default = true, show = { settings -> settings["NonCategory.CommandKeys"] as Boolean }),
            ToggleDef(id = "CooldownMessage", name = "[CK] CDMessage", show = { settings -> settings["NonCategory.CommandKeys"] as Boolean }),
            ToggleDef(name = "EfficientDB")
            )
        ),
        SubcategoryDef(id = "DisableUse", name = "Disable Use", category = "-NA General-", features = listOf(
            ToggleDef(name = "Second SoulSand"),
            ToggleDef(name = "Place Tuba"),
            ToggleDef(name = "Place BOL"),
            ToggleDef(name = "Place Sceptre"),
            ToggleDef(name = "Place Head"),
            ToggleDef(name = "SBMenu")
            )
        ),
        SubcategoryDef(id = "ChatHider", name = "Chat Hider", category = "-NA General-", features = listOf(
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
        //Dungeon
        SubcategoryDef(id = "PartyFinder", name = "Party Finder", category = "-NA Dungeon-"),
        SubcategoryDef(id = "AutoRefill", name = "Auto Refill", category = "-NA Dungeon-"),
        SubcategoryDef(id = "RenderHighlight", name = "Render Highlight", category = "-NA Dungeon-", features = listOf(
            ToggleDef(name = "Secret Item"),
            ColorPickerDef(id = "SecretItemColor", name = "[SI] Color", default = Color(0, 255, 0, 255), show = { settings -> settings["RenderHighlight.SecretItem"] as Boolean }),
            StepSliderDef(id = "SecretItemScale", name = "[SI] Scale", min = 0, max = 6, step = 1, default = 2, show = { settings -> settings["RenderHighlight.SecretItem"] as Boolean }),
            ToggleDef(name = "Bat"),
            ColorPickerDef(id = "BatLineColor", name = "[Ba] Line Color", default = Color(255, 0, 255, 255), show = { settings -> settings["RenderHighlight.Bat"] as Boolean }),
            ColorPickerDef(id = "BatFillColor", name = "[Ba] Fill Color", default = Color(255, 0, 255, 153), show = { settings -> settings["RenderHighlight.Bat"] as Boolean }),
            ToggleDef(id = "BatTracer", name = "[Ba] Tracer", show = { settings -> settings["RenderHighlight.Bat"] as Boolean }),
            ColorPickerDef(id = "BatTracerColor", name = "[Ba:T] Color", default = Color(255, 0, 255, 255), show = { settings -> settings["RenderHighlight.BatTracer"] as Boolean }),
            StepSliderDef(id = "BatScale", name = "[Ba] Scale", min = 0, max = 6, step = 1, default = 0, show = { settings -> settings["RenderHighlight.Bat"] as Boolean }),
            ToggleDef(name = "Wither"),
            ColorPickerDef(id = "WitherLineColor", name = "[Wi] Line Color", default = Color(0, 255, 255, 255), show = { settings -> settings["RenderHighlight.Wither"] as Boolean }),
            ColorPickerDef(id = "WitherFillColor", name = "[Wi] Fill Color", default = Color(0, 255, 255, 153), show = { settings -> settings["RenderHighlight.Wither"] as Boolean }),
            StepSliderDef(id = "WitherScale", name = "[Wi] Scale", min = 0, max = 6, step = 1, default = 0, show = { settings -> settings["RenderHighlight.Wither"] as Boolean }),
            ToggleDef(id = "WitherGoldorTracer", name = "[Wi] Goldor Tracer", show = { settings -> settings["RenderHighlight.Wither"] as Boolean }),
            ColorPickerDef(id = "WitherGoldorTracerColor", name = "[Wi:GT] Color", default = Color(255, 255, 0, 255), show = { settings -> settings["RenderHighlight.WitherGoldorTracer"] as Boolean }),
            ToggleDef(name = "Mimic Chest"),
            ColorPickerDef(id = "MimicChestLineColor", name = "[MC] Line Color", default = Color(255, 0, 0, 255), show = { settings -> settings["RenderHighlight.MimicChest"] as Boolean }),
            ColorPickerDef(id = "MimicChestFillColor", name = "[MC] Fill Color", default = Color(255, 0, 0, 153), show = { settings -> settings["RenderHighlight.MimicChest"] as Boolean }),
            ToggleDef(name = "Starred Mob"),
            ToggleDef(id = "StarredMobFill", name = "[SM] Fill", show = { settings -> settings["RenderHighlight.StarredMob"] as Boolean }),
            ColorPickerDef(id = "StarredMobLineColor", name = "[SM] Line Color", default = Color(0, 255, 0, 255), show = { settings -> settings["RenderHighlight.StarredMob"] as Boolean }),
            ColorPickerDef(id = "StarredMobFillColor", name = "[SM] Fill Color", default = Color(0, 255, 0, 153), show = { settings -> settings["RenderHighlight.StarredMobFill"] as Boolean }),
            StepSliderDef(id = "StarredMobScale", name = "[SM] Scale", min = 0, max = 6, step = 1, default = 3, show = { settings -> settings["RenderHighlight.StarredMob"] as Boolean })
            )
        ),
        SubcategoryDef(id = "Notification", name = "Notification", category = "-NA Dungeon-", features = listOf(
            ButtonDef(name = "Custom", onClick = { Screen.open("Notification") }),
            ToggleDef(name = "Enraged Wish"),
            ToggleDef(name = "Gate Broke"),
            ToggleDef(name = "Core Leap"),
            ToggleDef(name = "Necron Leap"),
            ToggleDef(name = "Ragnarock"),
            ToggleDef(name = "Chest Lock"),
            ToggleDef(name = "Mask"),
            ToggleDef(name = "Key Pick")
            )
        ),
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
            stellaNavExtra = created.getValue("Extra"),
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
    val stellaNav get() = FeatureConfig.sharedSections.stellaNavExtra
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

private fun ConfigSubcategory.boolean(key: String): Boolean =
    elements[key]?.value as Boolean

private fun ConfigSubcategory.string(key: String): String =
    elements[key]?.value as String

private fun ConfigSubcategory.int(key: String): Int =
    elements[key]?.value as Int

private fun ConfigSubcategory.float(key: String): Float =
    elements[key]?.value as Float

private fun ConfigSubcategory.color(key: String): Color =
    elements[key]?.value as Color

object StellaNavExtra {
    private val category get() = FeatureConfig.sharedSections.stellaNavExtra

    val DoorsFill get() = category.boolean("Extra.DoorsFill")
    val DoorsFillKeyColor get() = category.color("Extra.DoorsFillKeyColor")
    val DoorsFillNoKeyColor get() = category.color("Extra.DoorsFillNoKeyColor")
    val DoorsTracer get() = category.boolean("Extra.DoorsTracer")
    val DoorsTracerKeyColor get() = category.color("Extra.DoorsTracerKeyColor")
    val DoorsTracerNoKeyColor get() = category.color("Extra.DoorsTracerNoKeyColor")
}

object NonCategory {
    private val category get() = FeatureConfig.sharedSections.nonCategory

    val CommandKeys get() = category.boolean("NonCategory.CommandKeys")
    val EnabledCooldown get() = category.boolean("NonCategory.EnableCooldown")
    val CooldownMessage get() = category.boolean("NonCategory.CooldownMessage")
    val EfficientDB get() = category.boolean("NonCategory.EfficientDB")
}

object DisableUse {
    private val category get() = FeatureConfig.sharedSections.disableUse

    val SecondSoulSand get() = category.boolean("DisableUse.SecondSoulSand")
    val PlaceTuba get() = category.boolean("DisableUse.PlaceTuba")
    val PlaceBOL get() = category.boolean("DisableUse.PlaceBOL")
    val PlaceSceptre get() = category.boolean("DisableUse.PlaceSceptre")
    val PlaceHead get() = category.boolean("DisableUse.PlaceHead")
    val SBMenu get() = category.boolean("DisableUse.SBMenu")
}

object ChatHider {
    private val category get() = FeatureConfig.sharedSections.chatHider

    val Obtained get() = category.boolean("ChatHider.Obtained")
    val Milestone get() = category.boolean("ChatHider.Milestone")
    val KillCombo get() = category.boolean("ChatHider.KillCombo")
    val Boss get() = category.boolean("ChatHider.Boss")
    val NPCMort get() = category.boolean("ChatHider.NPCMort")
    val TeleportCooldown get() = category.boolean("ChatHider.TeleportCooldown")
    val Implosion get() = category.boolean("ChatHider.Implosion")
    val TrapRoom get() = category.boolean("ChatHider.TrapRoom")
    val Lever get() = category.boolean("ChatHider.Lever")
    val Chest get() = category.boolean("ChatHider.Chest")
    val IcePath get() = category.boolean("ChatHider.IcePath")
    val MysticalForce get() = category.boolean("ChatHider.MysticalForce")
    val LostAdventure get() = category.boolean("ChatHider.LostAdventure")
    val Essence get() = category.boolean("ChatHider.Essence")
    val Blessing get() = category.boolean("ChatHider.Blessing")
}

object RenderHighlight {
    private val category get() = FeatureConfig.sharedSections.renderHighlight

    val SecretItem get() = category.boolean("RenderHighlight.SecretItem")
    val SecretItemColor get() = category.color("RenderHighlight.SecretItemColor")
    val SecretItemScale get() = category.int("RenderHighlight.SecretItemScale")
    val Bat get() = category.boolean("RenderHighlight.Bat")
    val BatLineColor get() = category.color("RenderHighlight.BatLineColor")
    val BatFillColor get() = category.color("RenderHighlight.BatFillColor")
    val BatTracer get() = category.boolean("RenderHighlight.BatTracer")
    val BatTracerColor get() = category.color("RenderHighlight.BatTracerColor")
    val BatScale get() = category.int("RenderHighlight.BatScale")
    val Wither get() = category.boolean("RenderHighlight.Wither")
    val WitherLineColor get() = category.color("RenderHighlight.WitherLineColor")
    val WitherFillColor get() = category.color("RenderHighlight.WitherFillColor")
    val WitherScale get() = category.int("RenderHighlight.WitherScale")
    val WitherGoldorTracer get() = category.boolean("RenderHighlight.WitherGoldorTracer")
    val WitherGoldorTracerColor get() = category.color("RenderHighlight.WitherGoldorTracerColor")
    val MimicChest get() = category.boolean("RenderHighlight.MimicChest")
    val MimicChestLineColor get() = category.color("RenderHighlight.MimicChestLineColor")
    val MimicChestFillColor get() = category.color("RenderHighlight.MimicChestFillColor")
    val StarredMob get() = category.boolean("RenderHighlight.StarredMob")
    val StarredMobFill get() = category.boolean("RenderHighlight.StarredMobFill")
    val StarredMobLineColor get() = category.color("RenderHighlight.StarredMobLineColor")
    val StarredMobFillColor get() = category.color("RenderHighlight.StarredMobFillColor")
    val StarredMobScale get() = category.int("RenderHighlight.StarredMobScale")
}

object Notification {
    private val category get() = FeatureConfig.sharedSections.notification

    val EnragedWish get() = category.boolean("Notification.EnragedWish")
    val GateBroke get() = category.boolean("Notification.GateBroke")
    val CoreLeap get() = category.boolean("Notification.CoreLeap")
    val NecronLeap get() = category.boolean("Notification.NecronLeap")
    val Ragnarock get() = category.boolean("Notification.Ragnarock")
    val ChestLock get() = category.boolean("Notification.ChestLock")
    val Mask get() = category.boolean("Notification.Mask")
    val KeyPick get() = category.boolean("Notification.KeyPick")
}