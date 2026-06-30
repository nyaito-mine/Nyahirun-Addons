package co.stellarskys.nyahirunaddons.config

import co.stellarskys.nyahirunaddons.features.FeatureConfig
import co.stellarskys.stella.api.config.core.Config
import co.stellarskys.stella.api.config.core.ConfigSubcategory
import co.stellarskys.stella.api.zenith.client
import co.stellarskys.stella.events.EventBus
import co.stellarskys.stella.events.core.TickEvent
import java.lang.reflect.Field

fun bindAddonSections(sections: FeatureConfig.Sections) {
    FeatureConfig.bindAddonSections(sections)
}

fun attachSync(addonConfig: Config, stellaConfig: Config, sections: FeatureConfig.Sections) {
    val keys = collectKeys(sections)
    val addonBindings = keys.associateWith { key -> MirrorBinding(addonConfig, key) }
    val stellaBindings = keys.associateWith { key -> MirrorBinding(stellaConfig, key) }

    addonConfig.registerListener { key, value ->
        val binding = stellaBindings[key] ?: return@registerListener
        if (value != null) binding.value = value
    }

    stellaConfig.registerListener { key, value ->
        val binding = addonBindings[key] ?: return@registerListener
        if (value != null) binding.value = value
    }
}

fun watchConfigClose(addonConfig: Config, stellaConfig: Config) {
    var wasOpen = false

    EventBus.on<TickEvent.Client> {
        val isOpen = currentScreenName() == "ConfigUI"
        if (wasOpen && !isOpen) {
            addonConfig.save()
            stellaConfig.save()
            addonConfig.clearConfigUiCache()
            stellaConfig.clearConfigUiCache()
        }
        wasOpen = isOpen
    }
}

private fun collectKeys(sections: FeatureConfig.Sections): List<String> {
    val subcategories = sections.javaClass.declaredFields.mapNotNull { field ->
        if (!ConfigSubcategory::class.java.isAssignableFrom(field.type)) return@mapNotNull null
        field.isAccessible = true
        field.get(sections) as? ConfigSubcategory
    }

    return subcategories.flatMap { subcategory ->
        buildList {
            if (subcategory.configName.isNotBlank()) add(subcategory.configName)
            addAll(subcategory.elements.keys)
        }
    }
}

private fun currentScreenName(): String? {
    val runtime = client
    val methods = runtime.javaClass.methods.asList() + runtime.javaClass.declaredMethods.asList()
    val method = methods.firstOrNull { it.name == "currentScreen" || it.name == "getCurrentScreen" || it.name == "screen" || it.name == "getScreen" }
    val screen = method?.invoke(runtime) ?: run {
        val fields = runtime.javaClass.fields.asList() + runtime.javaClass.declaredFields.asList()
        val field = fields.firstOrNull { it.name == "currentScreen" || it.name == "screen" } ?: return null
        field.isAccessible = true
        field.get(runtime)
    } ?: return null
    return screen.javaClass.simpleName
}

private fun Config.clearConfigUiCache() {
    val field: Field = Config::class.java.getDeclaredField("configUI")
    field.isAccessible = true
    field.set(this, null)
}

private class MirrorBinding(config: Config, key: String) {
    var value: Any by config.property<Any>(key)
}
