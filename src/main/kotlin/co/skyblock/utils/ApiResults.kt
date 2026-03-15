package co.skyblock.utils

import co.skyblock.utils.dungeon.api.Manager
import net.minecraft.world.item.ItemStack

data class ApiResults(
    val sbLevel: String?,
    val magicalPower: Int?,
    val cataLevel: Double?,
    val selectedClass: String?,
    val archerLevel: Double?,
    val berserkLevel: Double?,
    val mageLevel: Double?,
    val tankLevel: Double?,
    val healerLevel: Double?,
    val secrets: Int?,
    val secretAverage: Double?,
    val goldCollection: String?,
    val combatLevel: Double?,
    val farmingLevel: Double?,
    val fishingLevel: Double?,
    val miningLevel: Double?,
    val foragingLevel: Double?,
    val enchantingLevel: Double?,
    val alchemyLevel: Double?,
    val carpentryLevel: Double?,
    val tamingLevel: Double?,
    val huntingLevel: Double?,
    val skillAverage: Double?,
    val pbF7: String?,
    val pbM4: String?,
    val pbM5: String?,
    val pbM6: String?,
    val pbM7: String?,
    val pets: List<Manager.Pet>,
    val items: List<ItemStack>,
    val wardrobe: List<ItemStack>,
    val armor: List<ItemStack>,
    val wardrobeEquipped: Int?,
    val equipments: List<ItemStack>
)