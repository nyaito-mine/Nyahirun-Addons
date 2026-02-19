package co.skyblock.utils.dungeon

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemLore

object DBUtils {

    private const val ID = "id"

    fun isDungeonBreakerSkyBlock(stack: ItemStack?): Boolean {
        if (stack == null || stack.isEmpty) return false

        if (!stack.item.toString().contains("diamond_pickaxe"))
            return false

        val lore: ItemLore = stack.get(DataComponents.LORE) ?: return false

        for (line: Component in lore.lines()) {
            val s = line.string
            if (s.contains("Ability: Dungeon Breaker")) {
                return true
            }
        }
        return false
    }

    fun getCustomData(stack: ItemStack?): CompoundTag {
        if (stack == null) return CompoundTag()

        val tag = stack
            .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag()

        return tag.copy()
    }

    fun getItemId(stack: ItemStack?): String {
        val customData = getCustomData(stack)
        return customData.getString(ID).orElse("")
    }
}
