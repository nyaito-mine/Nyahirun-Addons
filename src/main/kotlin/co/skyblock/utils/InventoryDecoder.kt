package co.skyblock.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.io.ByteArrayInputStream
import java.util.*

object InventoryDecoder {

    fun decodeInventory(base64: String?): List<ItemStack> {

        if (base64.isNullOrEmpty()) return emptyList()

        return try {

            val decoded = Base64.getDecoder().decode(base64)

            val tag: CompoundTag = NbtIo.readCompressed(
                ByteArrayInputStream(decoded),
                NbtAccounter.unlimitedHeap()
            )

            val items = mutableListOf<ItemStack>()

            val list = tag.getList("i").orElse(ListTag())

            for (i in 0 until list.size) {
                val itemTag = list.getCompound(i).orElse(CompoundTag())
                val stack = itemTag.fromLegacyNbt() ?: Items.AIR.defaultInstance

                items.add(stack)
            }

            items

        } catch (_: Exception) {
            emptyList()
        }
    }
}