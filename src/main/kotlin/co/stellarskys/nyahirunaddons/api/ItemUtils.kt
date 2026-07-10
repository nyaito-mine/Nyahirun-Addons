package co.stellarskys.nyahirunaddons.api

import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemLore
import java.util.WeakHashMap

object ItemUtils {

    private const val ID = "id"
    private val uuid = WeakHashMap<ItemStack, String>()

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

    fun getUuid(item: ItemStack): String? {
        return uuid.getOrPut(item) {
            val nbt = item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            nbt.copyTag().getStringOr("uuid", "")
        }.ifEmpty { null }
    }

    fun isHolding(name: String): Boolean {
        val player = Minecraft.getInstance().player ?: return false

        return player.mainHandItem.hoverName.string.contains(name)
    }

    fun itemHasName(itemStack: ItemStack?, name: String): Boolean {
        val player = Minecraft.getInstance().player
        if (player == null || itemStack == null) return false

        return itemStack.hoverName.string.contains(name)
    }

    fun containsLore(item: ItemStack?, contain: String): Boolean {
        if (item == null) return false
        val lore = item.get(DataComponents.LORE) ?: return false

        val lines = lore.lines()
        if (lines.isEmpty()) return false

        for (line in lines.reversed()) {
            val string = line.string
            if (string.contains(contain)) {
                return true
            }
        }
        return false
    }
}