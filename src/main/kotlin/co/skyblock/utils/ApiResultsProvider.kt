package co.skyblock.utils

import co.skyblock.utils.InventoryDecoder.decodeInventory
import co.skyblock.utils.dungeon.api.Manager
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import java.util.concurrent.ConcurrentHashMap

object ApiResultsProvider {

    private data class CachedApiResults(
        val signature: Int,
        val results: ApiResults
    )

    private val cache = ConcurrentHashMap<String, CachedApiResults>()

    fun getAPIResults(playerName: String): ApiResults {

        val sbLevel = Manager.getCachedSBLevel(playerName)
        val magicalPower = Manager.getCachedMagicalPower(playerName)
        val cataLevel = Manager.getCachedCataLevel(playerName)
        val selectedClass = Manager.getCachedSelectedClass(playerName)
        val archerLevel = Manager.getCachedClassLevel(playerName, "class_Archer")
        val berserkLevel = Manager.getCachedClassLevel(playerName, "class_Berserk")
        val mageLevel = Manager.getCachedClassLevel(playerName, "class_Mage")
        val tankLevel = Manager.getCachedClassLevel(playerName, "class_Tank")
        val healerLevel = Manager.getCachedClassLevel(playerName, "class_Healer")
        val secrets = Manager.getCachedSecret(playerName)
        val secretAverage = Manager.getCachedSecretAve(playerName)
        val goldCollection = Manager.getCachedGoldCollection(playerName)
        val combatLevel = Manager.getCachedSkillLevel(playerName, "skill_Combat")
        val farmingLevel = Manager.getCachedSkillLevel(playerName, "skill_Farming")
        val fishingLevel = Manager.getCachedSkillLevel(playerName, "skill_Fishing")
        val miningLevel = Manager.getCachedSkillLevel(playerName, "skill_Mining")
        val foragingLevel = Manager.getCachedSkillLevel(playerName, "skill_Foraging")
        val enchantingLevel = Manager.getCachedSkillLevel(playerName, "skill_Enchanting")
        val alchemyLevel = Manager.getCachedSkillLevel(playerName, "skill_Alchemy")
        val carpentryLevel = Manager.getCachedSkillLevel(playerName, "skill_Carpentry")
        val tamingLevel = Manager.getCachedSkillLevel(playerName, "skill_Taming")
        val huntingLevel = Manager.getCachedSkillLevel(playerName, "skill_Hunting")
        val skillAverage = Manager.getCachedSkillLevel(playerName, "skill_SkillAverage")

        val pbF7 = Manager.getCachedPBString(playerName, "The Catacombs", "floor_7")
        val pbM4 = Manager.getCachedPBString(playerName, "Master Mode The Catacombs", "floor_4")
        val pbM5 = Manager.getCachedPBString(playerName, "Master Mode The Catacombs", "floor_5")
        val pbM6 = Manager.getCachedPBString(playerName, "Master Mode The Catacombs", "floor_6")
        val pbM7 = Manager.getCachedPBString(playerName, "Master Mode The Catacombs", "floor_7")

        val pets = Manager.getCachedPets(playerName) ?: emptyList()
        val inventories = Manager.getCachedInventories(playerName)
        val signature = listOf(
            sbLevel,
            magicalPower,
            cataLevel,
            selectedClass,
            archerLevel,
            berserkLevel,
            mageLevel,
            tankLevel,
            healerLevel,
            secrets,
            secretAverage,
            goldCollection,
            combatLevel,
            farmingLevel,
            fishingLevel,
            miningLevel,
            foragingLevel,
            enchantingLevel,
            alchemyLevel,
            carpentryLevel,
            tamingLevel,
            huntingLevel,
            skillAverage,
            pbF7,
            pbM4,
            pbM5,
            pbM6,
            pbM7,
            pets,
            inventories?.inventory,
            inventories?.enderchest,
            inventories?.backpacks,
            inventories?.armor,
            inventories?.wardrobe,
            inventories?.wardrobeEquipped,
            inventories?.equipment
        ).hashCode()

        cache[playerName]?.takeIf { it.signature == signature }?.let {
            return it.results
        }

        val inventoryItems = decodeInventory(inventories?.inventory)
        val enderchestItems = decodeInventory(inventories?.enderchest)
        val backpacks = inventories?.backpacks
        val backpackItems = mutableListOf<ItemStack>()
        backpacks?.values?.forEach {
            backpackItems.addAll(decodeInventory(it))
        }
        val allItems = inventoryItems + enderchestItems + backpackItems
        val items = extractTargetItems(allItems)
            .sortedBy { getSkyblockId(it) ?: "" }
        val armor = decodeInventory(inventories?.armor)
        val wardrobe = decodeInventory(inventories?.wardrobe)
        val wardrobeEquipped = inventories?.wardrobeEquipped
        val equipments = decodeInventory(inventories?.equipment)

        val results = ApiResults(
            sbLevel,
            magicalPower,
            cataLevel,
            selectedClass,
            archerLevel,
            berserkLevel,
            mageLevel,
            tankLevel,
            healerLevel,
            secrets,
            secretAverage,
            goldCollection,
            combatLevel,
            farmingLevel,
            fishingLevel,
            miningLevel,
            foragingLevel,
            enchantingLevel,
            alchemyLevel,
            carpentryLevel,
            tamingLevel,
            huntingLevel,
            skillAverage,
            pbF7,
            pbM4,
            pbM5,
            pbM6,
            pbM7,
            pets,
            items,
            wardrobe,
            armor,
            wardrobeEquipped,
            equipments
        )

        cache[playerName] = CachedApiResults(signature, results)
        return results
    }

    val TARGET_ITEMS = setOf(
        "DARK_CLAYMORE",
        "HYPERION",
        "RAGNAROCK_AXE",
        "TERMINATOR"
    )

    fun extractTargetItems(items: List<ItemStack>): List<ItemStack> {
        val result = mutableListOf<ItemStack>()
        for (stack in items) {
            val id = getSkyblockId(stack) ?: continue
            if (id in TARGET_ITEMS) {
                result.add(stack)
            }
        }
        return result
    }

    fun getSkyblockId(stack: ItemStack): String? {
        val customData = stack.get(DataComponents.CUSTOM_DATA) ?: return null
        val tag = customData.copyTag()

        val extra = tag.getCompound("ExtraAttributes")
        if (extra.isPresent) {
            val extraId = extra.get().getString("id").orElse(null)
            if (!extraId.isNullOrEmpty()) return extraId
        }

        return tag.getString("id").orElse(null)
    }
}