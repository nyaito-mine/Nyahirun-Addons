@file:Suppress("DEPRECATION")

package co.skyblock.utils

import com.google.common.collect.LinkedHashMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.ints.IntLists
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.util.Unit
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.component.WrittenBookContent
import net.minecraft.world.level.block.entity.BannerPattern
import net.minecraft.world.level.block.entity.BannerPatternLayers
import net.minecraft.world.level.block.entity.BannerPatternLayers.Layer
import net.minecraft.world.level.saveddata.maps.MapId
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

// ═══════════════════════════════════════════════════════════════════════════════
// エントリーポイント
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Minecraft 1.8 以前の旧 NBT フォーマット（数値 id + Damage メタ値）から
 * 現代の [ItemStack] へ変換する。
 *
 * 変換フロー:
 * 1. [LegacyBaseItem] で旧数値 id + Damage → 現代 [Item] へ解決
 * 2. NBT `"tag"` 以下に対して [legacyFixers] を順番に適用し、
 *    旧コンポーネント形式を現代の DataComponent 形式へ変換
 * 3. [ItemStack] を組み立てて返す
 *
 * @return 変換後の [ItemStack]。tag が空なら [ItemStack.EMPTY]。
 *         id が未知の場合は `null`（呼び出し側で [Items.BARRIER] 等に差し替える）。
 */
internal fun Tag.fromLegacyNbt(): ItemStack? {
    if (this !is CompoundTag || this.isEmpty) return ItemStack.EMPTY

    val base = LegacyBaseItem.resolve(this) ?: return null
    val (item, count, builder) = base

    this.getCompound("tag").ifPresent { nbtTag ->
        legacyFixers.forEach { fixer ->
            if (fixer.canApply(item)) fixer.apply(builder, nbtTag)
        }
    }

    return ItemStack(item.builtInRegistryHolder(), count, builder.build())
}

// ═══════════════════════════════════════════════════════════════════════════════
// Fixer リスト（適用順序に意味があるものもあるため順序を維持する）
// ═══════════════════════════════════════════════════════════════════════════════

private val legacyFixers: List<DataComponentFixer<*>> = listOf(
    LegacyHideFlagsFixer,
    LegacySkullTextureFixer,
    LegacyLoreFixer,
    LegacyNameFixer,
    LegacyColorFixer,
    LegacyUnbreakableFixer,
    LegacyEnchantGlintFixer,
    LegacyWrittenBookFixer,
    LegacyBannerItemFixer,
    LegacyExtraAttributesFixer,
    LegacyFireworkExplosionFixer,
    LegacyItemModelFixer,
    LegacyRemoveFixer("overrideMeta"),
    LegacyRemoveFixer("AttributeModifiers"),
)

// ═══════════════════════════════════════════════════════════════════════════════
// DataComponentFixer インターフェース
// ═══════════════════════════════════════════════════════════════════════════════

private interface DataComponentFixer<T : Any> {
    val type: DataComponentType<T>

    /** NBT タグからデータを取得しつつ、処理済みのキーを remove する。null なら何もしない。 */
    fun getData(tag: CompoundTag): T?

    /** このアイテムに適用できるか（デフォルトは全アイテム対象）*/
    fun canApply(item: Item): Boolean = true

    fun apply(components: DataComponentPatch.Builder, tag: CompoundTag) {
        getData(tag)?.let { components.set(type, it) }
    }

    // ── CompoundTag ヘルパー ───────────────────────────────────────────────

    fun CompoundTag.removeIfEmpty(path: String) {
        if (this.getCompoundOrEmpty(path).isEmpty) this.remove(path)
    }

    fun CompoundTag.getAndRemove(path: String): Tag? = this.get(path).also { this.remove(path) }
    fun CompoundTag.getAndRemoveCompound(key: String): CompoundTag? = getAndRemove(key)?.asCompound()?.getOrNull()
    fun CompoundTag.getAndRemoveIntArray(key: String): IntArray? = getAndRemove(key)?.asIntArray()?.getOrNull()
    fun CompoundTag.getAndRemoveBoolean(key: String): Boolean? = getAndRemove(key)?.asBoolean()?.getOrNull()
    fun CompoundTag.getAndRemoveString(key: String): String? = getAndRemove(key)?.asString()?.getOrNull()
    fun CompoundTag.getAndRemoveList(key: String): ListTag? = getAndRemove(key)?.asList()?.getOrNull()
    fun CompoundTag.getAndRemoveByte(key: String): Byte? = getAndRemove(key)?.asByte()?.getOrNull()
    fun CompoundTag.getAndRemoveInt(key: String): Int? = getAndRemove(key)?.asInt()?.getOrNull()
}

// ═══════════════════════════════════════════════════════════════════════════════
// §-コード テキストパーサー
// ═══════════════════════════════════════════════════════════════════════════════

private object LegacyTextFixer {
    private val EMPTY: Style = Style.EMPTY.withItalic(false)
    private const val CONTROL_CHAR = '§'

    private val codeMap: Map<Char, Style.() -> Style> = buildMap {
        ChatFormatting.entries.filter { it.isColor }.forEach { f ->
            put(f.char.lowercaseChar()) { EMPTY.withColor(f) }
        }
        put(ChatFormatting.BOLD.char.lowercaseChar()) { withBold(true) }
        put(ChatFormatting.ITALIC.char.lowercaseChar()) { withItalic(true) }
        put(ChatFormatting.STRIKETHROUGH.char.lowercaseChar()) { withStrikethrough(true) }
        put(ChatFormatting.UNDERLINE.char.lowercaseChar()) { withUnderlined(true) }
        put(ChatFormatting.OBFUSCATED.char.lowercaseChar()) { withObfuscated(true) }
        put(ChatFormatting.RESET.char.lowercaseChar()) { EMPTY }
    }

    fun parse(text: String): Component = Component.empty().apply {
        if (!text.contains(CONTROL_CHAR)) { append(text); return@apply }

        var last: Style = EMPTY
        val reader = LegacyStringReader(text)
        append(reader.readUntil(CONTROL_CHAR))

        while (reader.canRead()) {
            codeMap[reader.read().lowercaseChar()]?.let { last = last.it() }
            if (reader.peek() == CONTROL_CHAR) { reader.skip(); continue }
            append(Component.literal(reader.readUntil(CONTROL_CHAR)).withStyle(last))
            last = EMPTY
        }
    }
}

private data class LegacyStringReader(val text: String) {
    var cursor = 0
    private val maxIndex = text.length - 1

    fun canRead() = cursor <= maxIndex
    fun peek(): Char? = if (canRead()) text[cursor] else null
    fun read(): Char = text[cursor++]
    fun skip() { cursor++ }
    fun readUntil(terminator: Char): String = buildString {
        while (canRead() && peek() != terminator) append(read())
        skip()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BasePotionItem  —  ポーション meta → PotionContents
// ═══════════════════════════════════════════════════════════════════════════════

private object LegacyBasePotionItem {
    private val potions = Int2ObjectOpenHashMap<Holder<Potion>>().apply {
        put(0, Potions.WATER); put(1, Potions.REGENERATION); put(2, Potions.SWIFTNESS)
        put(3, Potions.FIRE_RESISTANCE); put(4, Potions.POISON); put(5, Potions.HEALING)
        put(6, Potions.NIGHT_VISION); put(8, Potions.WEAKNESS); put(9, Potions.STRENGTH)
        put(10, Potions.SLOWNESS); put(11, Potions.LEAPING); put(12, Potions.HARMING)
        put(13, Potions.WATER_BREATHING); put(14, Potions.INVISIBILITY)
        put(16, Potions.AWKWARD); put(17, Potions.REGENERATION); put(18, Potions.SWIFTNESS)
        put(19, Potions.FIRE_RESISTANCE); put(20, Potions.POISON); put(21, Potions.HEALING)
        put(22, Potions.NIGHT_VISION); put(24, Potions.WEAKNESS); put(25, Potions.STRENGTH)
        put(26, Potions.SLOWNESS); put(27, Potions.LEAPING); put(28, Potions.HARMING)
        put(29, Potions.WATER_BREATHING); put(30, Potions.INVISIBILITY)
        put(32, Potions.THICK); put(33, Potions.STRONG_REGENERATION); put(34, Potions.STRONG_SWIFTNESS)
        put(35, Potions.FIRE_RESISTANCE); put(36, Potions.STRONG_POISON); put(37, Potions.STRONG_HEALING)
        put(38, Potions.NIGHT_VISION); put(40, Potions.WEAKNESS); put(41, Potions.STRONG_STRENGTH)
        put(42, Potions.SLOWNESS); put(43, Potions.STRONG_LEAPING); put(44, Potions.STRONG_HARMING)
        put(45, Potions.WATER_BREATHING); put(46, Potions.INVISIBILITY)
        put(49, Potions.STRONG_REGENERATION); put(50, Potions.STRONG_SWIFTNESS)
        put(51, Potions.FIRE_RESISTANCE); put(52, Potions.STRONG_POISON); put(53, Potions.STRONG_HEALING)
        put(54, Potions.NIGHT_VISION); put(56, Potions.WEAKNESS); put(57, Potions.STRONG_STRENGTH)
        put(58, Potions.SLOWNESS); put(59, Potions.STRONG_LEAPING); put(60, Potions.STRONG_HARMING)
        put(61, Potions.WATER_BREATHING); put(62, Potions.INVISIBILITY)
        put(64, Potions.MUNDANE); put(65, Potions.LONG_REGENERATION); put(66, Potions.LONG_SWIFTNESS)
        put(67, Potions.LONG_FIRE_RESISTANCE); put(68, Potions.LONG_POISON); put(69, Potions.HEALING)
        put(70, Potions.LONG_NIGHT_VISION); put(72, Potions.LONG_WEAKNESS); put(73, Potions.LONG_STRENGTH)
        put(74, Potions.LONG_SLOWNESS); put(75, Potions.LONG_LEAPING); put(76, Potions.HARMING)
        put(77, Potions.LONG_WATER_BREATHING); put(78, Potions.LONG_INVISIBILITY)
        put(80, Potions.AWKWARD); put(81, Potions.LONG_REGENERATION); put(82, Potions.LONG_SWIFTNESS)
        put(83, Potions.LONG_FIRE_RESISTANCE); put(84, Potions.LONG_POISON); put(85, Potions.HEALING)
        put(86, Potions.LONG_NIGHT_VISION); put(88, Potions.LONG_WEAKNESS); put(89, Potions.LONG_STRENGTH)
        put(90, Potions.LONG_SLOWNESS); put(91, Potions.LONG_LEAPING); put(92, Potions.HARMING)
        put(93, Potions.LONG_WATER_BREATHING); put(94, Potions.LONG_INVISIBILITY)
        put(96, Potions.THICK); put(97, Potions.REGENERATION); put(98, Potions.SWIFTNESS)
        put(99, Potions.LONG_FIRE_RESISTANCE); put(100, Potions.POISON); put(101, Potions.STRONG_HEALING)
        put(102, Potions.LONG_NIGHT_VISION); put(104, Potions.LONG_WEAKNESS); put(105, Potions.STRENGTH)
        put(106, Potions.LONG_SLOWNESS); put(107, Potions.LEAPING); put(108, Potions.STRONG_HARMING)
        put(109, Potions.LONG_WATER_BREATHING); put(110, Potions.LONG_INVISIBILITY)
        put(113, Potions.REGENERATION); put(114, Potions.SWIFTNESS)
        put(115, Potions.LONG_FIRE_RESISTANCE); put(116, Potions.POISON); put(117, Potions.STRONG_HEALING)
        put(118, Potions.LONG_NIGHT_VISION); put(120, Potions.LONG_WEAKNESS); put(121, Potions.STRENGTH)
        put(122, Potions.LONG_SLOWNESS); put(123, Potions.LEAPING); put(124, Potions.STRONG_HARMING)
        put(125, Potions.LONG_WATER_BREATHING); put(126, Potions.LONG_INVISIBILITY)
        defaultReturnValue(Potions.WATER)
    }
    private val cache = ConcurrentHashMap<Int, PotionContents>()

    /** meta から (Item, PotionContents) を返す。meta bit14 が立っていればスプラッシュポーション。 */
    fun resolve(meta: Int, tag: CompoundTag?): Pair<Item, PotionContents> {
        tag?.remove("CustomPotionEffects")
        val item = if (meta.and(16384) == 16384) Items.SPLASH_POTION else Items.POTION
        val contents = cache.getOrPut(meta.and(127)) { PotionContents(potions[meta.and(127)] ?: Potions.WATER) }
        return item to contents
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BaseItem  —  旧数値 id + Damage meta → 現代 Item へのマッピング
// ═══════════════════════════════════════════════════════════════════════════════

private object LegacyBaseItem {
    /** key = id, value = (meta → Item) マップ。meta 不問のアイテムは defaultReturnValue で設定する。 */
    private val items = Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Item>>()

    /** ダメージ値を使うアイテム（meta が耐久値になるもの）*/
    private val damageable = setOf(
        Items.BOW, Items.CARROT_ON_A_STICK, Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_CHESTPLATE,
        Items.CHAINMAIL_HELMET, Items.CHAINMAIL_LEGGINGS, Items.DIAMOND_AXE, Items.DIAMOND_BOOTS,
        Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET, Items.DIAMOND_HOE, Items.DIAMOND_LEGGINGS,
        Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD, Items.ELYTRA,
        Items.FISHING_ROD, Items.FLINT_AND_STEEL, Items.GOLDEN_AXE, Items.GOLDEN_BOOTS,
        Items.GOLDEN_CHESTPLATE, Items.GOLDEN_HELMET, Items.GOLDEN_HOE, Items.GOLDEN_LEGGINGS,
        Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD, Items.IRON_AXE,
        Items.IRON_BOOTS, Items.IRON_CHESTPLATE, Items.IRON_HELMET, Items.IRON_HOE,
        Items.IRON_LEGGINGS, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_SWORD,
        Items.LEATHER_BOOTS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET, Items.LEATHER_LEGGINGS,
        Items.SHEARS, Items.SHIELD, Items.STONE_AXE, Items.STONE_HOE, Items.STONE_PICKAXE,
        Items.STONE_SHOVEL, Items.STONE_SWORD, Items.WOODEN_AXE, Items.WOODEN_HOE,
        Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD,
    )

    init {
        // ── ブロック (id 1–192) ───────────────────────────────────────────────
        complex(1, 0, Items.STONE); complex(1, 1, Items.GRANITE); complex(1, 2, Items.POLISHED_GRANITE)
        complex(1, 3, Items.DIORITE); complex(1, 4, Items.POLISHED_DIORITE); complex(1, 5, Items.ANDESITE)
        complex(1, 6, Items.POLISHED_ANDESITE)
        normal(2, Items.GRASS_BLOCK)
        complex(3, 0, Items.DIRT); complex(3, 1, Items.COARSE_DIRT); complex(3, 2, Items.PODZOL)
        normal(4, Items.COBBLESTONE)
        complex(5, 0, Items.OAK_PLANKS); complex(5, 1, Items.SPRUCE_PLANKS); complex(5, 2, Items.BIRCH_PLANKS)
        complex(5, 3, Items.JUNGLE_PLANKS); complex(5, 4, Items.ACACIA_PLANKS); complex(5, 5, Items.DARK_OAK_PLANKS)
        complex(6, 0, Items.OAK_SAPLING); complex(6, 1, Items.SPRUCE_SAPLING); complex(6, 2, Items.BIRCH_SAPLING)
        complex(6, 3, Items.JUNGLE_SAPLING); complex(6, 4, Items.ACACIA_SAPLING); complex(6, 5, Items.DARK_OAK_SAPLING)
        normal(7, Items.BEDROCK)
        normal(8, Items.WATER_BUCKET); normal(9, Items.WATER_BUCKET)
        normal(10, Items.LAVA_BUCKET); normal(11, Items.LAVA_BUCKET)
        complex(12, 0, Items.SAND); complex(12, 1, Items.RED_SAND)
        normal(13, Items.GRAVEL); normal(14, Items.GOLD_ORE); normal(15, Items.IRON_ORE); normal(16, Items.COAL_ORE)
        complex(17, 0, Items.OAK_LOG); complex(17, 1, Items.SPRUCE_LOG); complex(17, 2, Items.BIRCH_LOG); complex(17, 3, Items.JUNGLE_LOG)
        complex(18, 0, Items.OAK_LEAVES); complex(18, 1, Items.SPRUCE_LEAVES); complex(18, 2, Items.BIRCH_LEAVES); complex(18, 3, Items.JUNGLE_LEAVES)
        complex(19, 0, Items.SPONGE); complex(19, 1, Items.WET_SPONGE)
        normal(20, Items.GLASS); normal(21, Items.LAPIS_ORE); normal(22, Items.LAPIS_BLOCK); normal(23, Items.DISPENSER)
        complex(24, 0, Items.SANDSTONE); complex(24, 1, Items.CHISELED_SANDSTONE); complex(24, 2, Items.CUT_SANDSTONE)
        normal(25, Items.NOTE_BLOCK)
        // ベッド (26 / 355)
        for ((meta, item) in listOf(0 to Items.WHITE_BED, 1 to Items.ORANGE_BED, 2 to Items.MAGENTA_BED,
            3 to Items.LIGHT_BLUE_BED, 4 to Items.YELLOW_BED, 5 to Items.LIME_BED, 6 to Items.PINK_BED,
            7 to Items.GRAY_BED, 8 to Items.LIGHT_GRAY_BED, 9 to Items.CYAN_BED, 10 to Items.PURPLE_BED,
            11 to Items.BLUE_BED, 12 to Items.BROWN_BED, 13 to Items.GREEN_BED, 14 to Items.RED_BED, 15 to Items.BLACK_BED)) {
            complex(26, meta, item); complex(355, meta, item)
        }
        normal(27, Items.POWERED_RAIL); normal(28, Items.DETECTOR_RAIL); normal(29, Items.STICKY_PISTON)
        normal(30, Items.COBWEB)
        complex(31, 0, Items.DEAD_BUSH); complex(31, 1, Items.SHORT_GRASS); complex(31, 2, Items.FERN)
        complex(32, 0, Items.DEAD_BUSH)
        normal(33, Items.PISTON); normal(34, Items.PISTON); normal(36, Items.PISTON)
        // ウール (35)
        for ((meta, item) in listOf(0 to Items.WHITE_WOOL, 1 to Items.ORANGE_WOOL, 2 to Items.MAGENTA_WOOL,
            3 to Items.LIGHT_BLUE_WOOL, 4 to Items.YELLOW_WOOL, 5 to Items.LIME_WOOL, 6 to Items.PINK_WOOL,
            7 to Items.GRAY_WOOL, 8 to Items.LIGHT_GRAY_WOOL, 9 to Items.CYAN_WOOL, 10 to Items.PURPLE_WOOL,
            11 to Items.BLUE_WOOL, 12 to Items.BROWN_WOOL, 13 to Items.GREEN_WOOL, 14 to Items.RED_WOOL, 15 to Items.BLACK_WOOL))
            complex(35, meta, item)
        normal(37, Items.DANDELION)
        complex(38, 0, Items.POPPY); complex(38, 1, Items.BLUE_ORCHID); complex(38, 2, Items.ALLIUM)
        complex(38, 3, Items.AZURE_BLUET); complex(38, 4, Items.RED_TULIP); complex(38, 5, Items.ORANGE_TULIP)
        complex(38, 6, Items.WHITE_TULIP); complex(38, 7, Items.PINK_TULIP); complex(38, 8, Items.OXEYE_DAISY)
        normal(39, Items.BROWN_MUSHROOM); normal(40, Items.RED_MUSHROOM)
        normal(41, Items.GOLD_BLOCK); normal(42, Items.IRON_BLOCK)
        normal(43, Items.STONE_SLAB)
        complex(44, 0, Items.STONE_SLAB); complex(44, 1, Items.SANDSTONE_SLAB); complex(44, 2, Items.PETRIFIED_OAK_SLAB)
        complex(44, 3, Items.COBBLESTONE_SLAB); complex(44, 4, Items.BRICK_SLAB); complex(44, 5, Items.STONE_BRICK_SLAB)
        complex(44, 6, Items.NETHER_BRICK_SLAB); complex(44, 7, Items.QUARTZ_SLAB)
        normal(45, Items.BRICKS); normal(46, Items.TNT); normal(47, Items.BOOKSHELF)
        normal(48, Items.MOSSY_COBBLESTONE); normal(49, Items.OBSIDIAN); normal(50, Items.TORCH)
        normal(51, Items.AIR); normal(52, Items.SPAWNER); normal(53, Items.OAK_STAIRS)
        normal(54, Items.CHEST); normal(55, Items.REDSTONE); normal(56, Items.DIAMOND_ORE)
        normal(57, Items.DIAMOND_BLOCK); normal(58, Items.CRAFTING_TABLE); normal(59, Items.WHEAT)
        normal(60, Items.FARMLAND); normal(61, Items.FURNACE); normal(62, Items.FURNACE)
        normal(63, Items.OAK_SIGN); normal(64, Items.OAK_DOOR); normal(65, Items.LADDER)
        normal(66, Items.RAIL); normal(67, Items.COBBLESTONE_STAIRS); normal(68, Items.OAK_SIGN)
        normal(69, Items.LEVER); normal(70, Items.STONE_PRESSURE_PLATE); normal(71, Items.IRON_DOOR)
        normal(72, Items.OAK_PRESSURE_PLATE); normal(73, Items.REDSTONE_ORE)
        normal(76, Items.REDSTONE_TORCH); normal(77, Items.STONE_BUTTON); normal(78, Items.SNOW)
        normal(79, Items.ICE); normal(80, Items.SNOW_BLOCK); normal(81, Items.CACTUS)
        normal(82, Items.CLAY); normal(83, Items.SUGAR_CANE); normal(84, Items.JUKEBOX)
        normal(85, Items.OAK_FENCE); normal(86, Items.CARVED_PUMPKIN); normal(87, Items.NETHERRACK)
        normal(88, Items.SOUL_SAND); normal(89, Items.GLOWSTONE); normal(90, Items.AIR)
        normal(91, Items.JACK_O_LANTERN); normal(92, Items.CAKE); normal(93, Items.REPEATER); normal(94, Items.REPEATER)
        // 染色ガラス (95)
        for ((meta, item) in listOf(0 to Items.WHITE_STAINED_GLASS, 1 to Items.ORANGE_STAINED_GLASS,
            2 to Items.MAGENTA_STAINED_GLASS, 3 to Items.LIGHT_BLUE_STAINED_GLASS, 4 to Items.YELLOW_STAINED_GLASS,
            5 to Items.LIME_STAINED_GLASS, 6 to Items.PINK_STAINED_GLASS, 7 to Items.GRAY_STAINED_GLASS,
            8 to Items.LIGHT_GRAY_STAINED_GLASS, 9 to Items.CYAN_STAINED_GLASS, 10 to Items.PURPLE_STAINED_GLASS,
            11 to Items.BLUE_STAINED_GLASS, 12 to Items.BROWN_STAINED_GLASS, 13 to Items.GREEN_STAINED_GLASS,
            14 to Items.RED_STAINED_GLASS, 15 to Items.BLACK_STAINED_GLASS))
            complex(95, meta, item)
        normal(96, Items.OAK_TRAPDOOR)
        complex(97, 0, Items.INFESTED_STONE); complex(97, 1, Items.INFESTED_COBBLESTONE)
        complex(97, 2, Items.INFESTED_STONE_BRICKS); complex(97, 3, Items.INFESTED_MOSSY_STONE_BRICKS)
        complex(97, 4, Items.INFESTED_CRACKED_STONE_BRICKS); complex(97, 5, Items.INFESTED_CHISELED_STONE_BRICKS)
        complex(98, 0, Items.STONE_BRICKS); complex(98, 1, Items.MOSSY_STONE_BRICKS)
        complex(98, 2, Items.CRACKED_STONE_BRICKS); complex(98, 3, Items.CHISELED_STONE_BRICKS)
        normal(99, Items.BROWN_MUSHROOM_BLOCK); normal(100, Items.RED_MUSHROOM_BLOCK)
        normal(101, Items.IRON_BARS); normal(102, Items.GLASS_PANE); normal(103, Items.MELON)
        normal(104, Items.MELON); normal(105, Items.PUMPKIN); normal(106, Items.VINE)
        normal(107, Items.OAK_FENCE_GATE); normal(108, Items.BRICK_STAIRS); normal(109, Items.STONE_BRICK_STAIRS)
        normal(110, Items.MYCELIUM); normal(111, Items.LILY_PAD); normal(112, Items.NETHER_BRICKS)
        normal(113, Items.NETHER_BRICK_FENCE); normal(114, Items.NETHER_BRICK_STAIRS); normal(115, Items.NETHER_WART)
        normal(116, Items.ENCHANTING_TABLE); normal(117, Items.BREWING_STAND); normal(118, Items.CAULDRON)
        normal(119, Items.AIR); normal(120, Items.END_PORTAL_FRAME); normal(121, Items.END_STONE)
        normal(122, Items.DRAGON_EGG); normal(123, Items.REDSTONE_LAMP); normal(124, Items.REDSTONE_LAMP)
        normal(125, Items.OAK_SLAB)
        complex(126, 0, Items.OAK_SLAB); complex(126, 1, Items.SPRUCE_SLAB); complex(126, 2, Items.BIRCH_SLAB)
        complex(126, 3, Items.JUNGLE_SLAB); complex(126, 4, Items.ACACIA_SLAB); complex(126, 5, Items.DARK_OAK_SLAB)
        normal(127, Items.COCOA_BEANS); normal(128, Items.SANDSTONE_STAIRS); normal(129, Items.EMERALD_ORE)
        normal(130, Items.ENDER_CHEST); normal(131, Items.TRIPWIRE_HOOK); normal(132, Items.STRING)
        normal(133, Items.EMERALD_BLOCK); normal(134, Items.SPRUCE_STAIRS); normal(135, Items.BIRCH_STAIRS)
        normal(136, Items.JUNGLE_STAIRS); normal(137, Items.COMMAND_BLOCK); normal(138, Items.BEACON)
        complex(139, 0, Items.COBBLESTONE_WALL); complex(139, 1, Items.MOSSY_COBBLESTONE_WALL)
        normal(140, Items.FLOWER_POT); normal(141, Items.CARROT); normal(142, Items.POTATO)
        normal(143, Items.OAK_BUTTON)
        complex(144, 0, Items.SKELETON_SKULL); complex(144, 1, Items.WITHER_SKELETON_SKULL)
        complex(144, 2, Items.ZOMBIE_HEAD); complex(144, 3, Items.PLAYER_HEAD)
        complex(144, 4, Items.CREEPER_HEAD); complex(144, 5, Items.DRAGON_HEAD)
        complex(145, 0, Items.ANVIL); complex(145, 1, Items.CHIPPED_ANVIL); complex(145, 2, Items.DAMAGED_ANVIL)
        normal(146, Items.TRAPPED_CHEST); normal(147, Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
        normal(148, Items.HEAVY_WEIGHTED_PRESSURE_PLATE); normal(149, Items.COMPARATOR); normal(150, Items.COMPARATOR)
        normal(151, Items.DAYLIGHT_DETECTOR); normal(152, Items.REDSTONE_BLOCK); normal(153, Items.NETHER_QUARTZ_ORE)
        normal(154, Items.HOPPER)
        complex(155, 0, Items.QUARTZ_BLOCK); complex(155, 1, Items.CHISELED_QUARTZ_BLOCK); complex(155, 2, Items.QUARTZ_PILLAR)
        normal(156, Items.QUARTZ_STAIRS); normal(157, Items.ACTIVATOR_RAIL); normal(158, Items.DROPPER)
        // 彩釉テラコッタ (159)
        for ((meta, item) in listOf(0 to Items.WHITE_TERRACOTTA, 1 to Items.ORANGE_TERRACOTTA,
            2 to Items.MAGENTA_TERRACOTTA, 3 to Items.LIGHT_BLUE_TERRACOTTA, 4 to Items.YELLOW_TERRACOTTA,
            5 to Items.LIME_TERRACOTTA, 6 to Items.PINK_TERRACOTTA, 7 to Items.GRAY_TERRACOTTA,
            8 to Items.LIGHT_GRAY_TERRACOTTA, 9 to Items.CYAN_TERRACOTTA, 10 to Items.PURPLE_TERRACOTTA,
            11 to Items.BLUE_TERRACOTTA, 12 to Items.BROWN_TERRACOTTA, 13 to Items.GREEN_TERRACOTTA,
            14 to Items.RED_TERRACOTTA, 15 to Items.BLACK_TERRACOTTA))
            complex(159, meta, item)
        // 染色ガラスペイン (160)
        for ((meta, item) in listOf(0 to Items.WHITE_STAINED_GLASS_PANE, 1 to Items.ORANGE_STAINED_GLASS_PANE,
            2 to Items.MAGENTA_STAINED_GLASS_PANE, 3 to Items.LIGHT_BLUE_STAINED_GLASS_PANE,
            4 to Items.YELLOW_STAINED_GLASS_PANE, 5 to Items.LIME_STAINED_GLASS_PANE,
            6 to Items.PINK_STAINED_GLASS_PANE, 7 to Items.GRAY_STAINED_GLASS_PANE,
            8 to Items.LIGHT_GRAY_STAINED_GLASS_PANE, 9 to Items.CYAN_STAINED_GLASS_PANE,
            10 to Items.PURPLE_STAINED_GLASS_PANE, 11 to Items.BLUE_STAINED_GLASS_PANE,
            12 to Items.BROWN_STAINED_GLASS_PANE, 13 to Items.GREEN_STAINED_GLASS_PANE,
            14 to Items.RED_STAINED_GLASS_PANE, 15 to Items.BLACK_STAINED_GLASS_PANE))
            complex(160, meta, item)
        complex(161, 0, Items.ACACIA_LEAVES); complex(161, 1, Items.DARK_OAK_LEAVES)
        complex(162, 0, Items.ACACIA_LOG); complex(162, 1, Items.DARK_OAK_LOG)
        normal(163, Items.ACACIA_STAIRS); normal(164, Items.DARK_OAK_STAIRS); normal(165, Items.SLIME_BLOCK)
        normal(166, Items.BARRIER); normal(167, Items.IRON_TRAPDOOR)
        complex(168, 0, Items.PRISMARINE); complex(168, 1, Items.PRISMARINE_BRICKS); complex(168, 2, Items.DARK_PRISMARINE)
        normal(169, Items.SEA_LANTERN); normal(170, Items.HAY_BLOCK)
        // カーペット (171)
        for ((meta, item) in listOf(0 to Items.WHITE_CARPET, 1 to Items.ORANGE_CARPET, 2 to Items.MAGENTA_CARPET,
            3 to Items.LIGHT_BLUE_CARPET, 4 to Items.YELLOW_CARPET, 5 to Items.LIME_CARPET,
            6 to Items.PINK_CARPET, 7 to Items.GRAY_CARPET, 8 to Items.LIGHT_GRAY_CARPET,
            9 to Items.CYAN_CARPET, 10 to Items.PURPLE_CARPET, 11 to Items.BLUE_CARPET,
            12 to Items.BROWN_CARPET, 13 to Items.GREEN_CARPET, 14 to Items.RED_CARPET, 15 to Items.BLACK_CARPET))
            complex(171, meta, item)
        normal(172, Items.TERRACOTTA); normal(173, Items.COAL_BLOCK); normal(174, Items.PACKED_ICE)
        complex(175, 0, Items.SUNFLOWER); complex(175, 1, Items.LILAC); complex(175, 2, Items.TALL_GRASS)
        complex(175, 3, Items.LARGE_FERN); complex(175, 4, Items.ROSE_BUSH); complex(175, 5, Items.PEONY)
        normal(176, Items.WHITE_BANNER); normal(177, Items.WHITE_BANNER); normal(178, Items.DAYLIGHT_DETECTOR)
        complex(179, 0, Items.RED_SANDSTONE); complex(179, 1, Items.CHISELED_RED_SANDSTONE); complex(179, 2, Items.CUT_RED_SANDSTONE)
        normal(180, Items.RED_SANDSTONE_STAIRS)
        complex(182, 0, Items.RED_SANDSTONE_SLAB)
        normal(183, Items.SPRUCE_FENCE_GATE); normal(184, Items.BIRCH_FENCE_GATE); normal(185, Items.JUNGLE_FENCE_GATE)
        normal(186, Items.DARK_OAK_FENCE_GATE); normal(187, Items.ACACIA_FENCE_GATE)
        normal(188, Items.SPRUCE_FENCE); normal(189, Items.BIRCH_FENCE); normal(190, Items.JUNGLE_FENCE)
        normal(191, Items.DARK_OAK_FENCE); normal(192, Items.ACACIA_FENCE)

        // ── アイテム (id 256–) ──────────────────────────────────────────────
        normal(256, Items.IRON_SHOVEL); normal(257, Items.IRON_PICKAXE); normal(258, Items.IRON_AXE)
        normal(259, Items.FLINT_AND_STEEL); normal(260, Items.APPLE); normal(261, Items.BOW)
        normal(262, Items.ARROW)
        complex(263, 0, Items.COAL); complex(263, 1, Items.CHARCOAL)
        normal(264, Items.DIAMOND); normal(265, Items.IRON_INGOT); normal(266, Items.GOLD_INGOT)
        normal(267, Items.IRON_SWORD); normal(268, Items.WOODEN_SWORD); normal(269, Items.WOODEN_SHOVEL)
        normal(270, Items.WOODEN_PICKAXE); normal(271, Items.WOODEN_AXE); normal(272, Items.STONE_SWORD)
        normal(273, Items.STONE_SHOVEL); normal(274, Items.STONE_PICKAXE); normal(275, Items.STONE_AXE)
        normal(276, Items.DIAMOND_SWORD); normal(277, Items.DIAMOND_SHOVEL); normal(278, Items.DIAMOND_PICKAXE)
        normal(279, Items.DIAMOND_AXE); normal(280, Items.STICK); normal(281, Items.BOWL)
        normal(282, Items.MUSHROOM_STEW); normal(283, Items.GOLDEN_SWORD); normal(284, Items.GOLDEN_SHOVEL)
        normal(285, Items.GOLDEN_PICKAXE); normal(286, Items.GOLDEN_AXE); normal(287, Items.STRING)
        normal(288, Items.FEATHER); normal(289, Items.GUNPOWDER); normal(290, Items.WOODEN_HOE)
        normal(291, Items.STONE_HOE); normal(292, Items.IRON_HOE); normal(293, Items.DIAMOND_HOE)
        normal(294, Items.GOLDEN_HOE); normal(295, Items.WHEAT_SEEDS); normal(296, Items.WHEAT)
        normal(297, Items.BREAD); normal(298, Items.LEATHER_HELMET); normal(299, Items.LEATHER_CHESTPLATE)
        normal(300, Items.LEATHER_LEGGINGS); normal(301, Items.LEATHER_BOOTS)
        normal(302, Items.CHAINMAIL_HELMET); normal(303, Items.CHAINMAIL_CHESTPLATE)
        normal(304, Items.CHAINMAIL_LEGGINGS); normal(305, Items.CHAINMAIL_BOOTS)
        normal(306, Items.IRON_HELMET); normal(307, Items.IRON_CHESTPLATE)
        normal(308, Items.IRON_LEGGINGS); normal(309, Items.IRON_BOOTS)
        normal(310, Items.DIAMOND_HELMET); normal(311, Items.DIAMOND_CHESTPLATE)
        normal(312, Items.DIAMOND_LEGGINGS); normal(313, Items.DIAMOND_BOOTS)
        normal(314, Items.GOLDEN_HELMET); normal(315, Items.GOLDEN_CHESTPLATE)
        normal(316, Items.GOLDEN_LEGGINGS); normal(317, Items.GOLDEN_BOOTS)
        normal(318, Items.FLINT); normal(319, Items.PORKCHOP); normal(320, Items.COOKED_PORKCHOP)
        normal(321, Items.PAINTING)
        complex(322, 0, Items.GOLDEN_APPLE); complex(322, 1, Items.ENCHANTED_GOLDEN_APPLE)
        normal(323, Items.OAK_SIGN); normal(324, Items.OAK_DOOR); normal(325, Items.BUCKET)
        normal(326, Items.WATER_BUCKET); normal(327, Items.LAVA_BUCKET); normal(328, Items.MINECART)
        normal(329, Items.SADDLE); normal(330, Items.IRON_DOOR); normal(331, Items.REDSTONE)
        normal(332, Items.SNOWBALL); normal(333, Items.OAK_BOAT); normal(334, Items.LEATHER)
        normal(335, Items.MILK_BUCKET); normal(336, Items.BRICK); normal(337, Items.CLAY_BALL)
        normal(338, Items.SUGAR_CANE); normal(339, Items.PAPER); normal(340, Items.BOOK)
        normal(341, Items.SLIME_BALL); normal(342, Items.CHEST_MINECART); normal(343, Items.FURNACE_MINECART)
        normal(344, Items.EGG); normal(345, Items.COMPASS); normal(346, Items.FISHING_ROD)
        normal(347, Items.CLOCK); normal(348, Items.GLOWSTONE_DUST)
        complex(349, 0, Items.COD); complex(349, 1, Items.SALMON); complex(349, 2, Items.TROPICAL_FISH); complex(349, 3, Items.PUFFERFISH)
        complex(350, 0, Items.COOKED_COD); complex(350, 1, Items.COOKED_SALMON)
        complex(351, 0, Items.INK_SAC); complex(351, 1, Items.RED_DYE); complex(351, 2, Items.GREEN_DYE)
        complex(351, 3, Items.COCOA_BEANS); complex(351, 4, Items.LAPIS_LAZULI); complex(351, 5, Items.PURPLE_DYE)
        complex(351, 6, Items.CYAN_DYE); complex(351, 7, Items.LIGHT_GRAY_DYE); complex(351, 8, Items.GRAY_DYE)
        complex(351, 9, Items.PINK_DYE); complex(351, 10, Items.LIME_DYE); complex(351, 11, Items.YELLOW_DYE)
        complex(351, 12, Items.LIGHT_BLUE_DYE); complex(351, 13, Items.MAGENTA_DYE)
        complex(351, 14, Items.ORANGE_DYE); complex(351, 15, Items.BONE_MEAL)
        normal(352, Items.BONE); normal(353, Items.SUGAR); normal(354, Items.CAKE)
        normal(356, Items.REPEATER); normal(357, Items.COOKIE); normal(358, Items.FILLED_MAP)
        normal(359, Items.SHEARS); normal(360, Items.MELON_SLICE); normal(361, Items.PUMPKIN_SEEDS)
        normal(362, Items.MELON_SEEDS); normal(363, Items.BEEF); normal(364, Items.COOKED_BEEF)
        normal(365, Items.CHICKEN); normal(366, Items.COOKED_CHICKEN); normal(367, Items.ROTTEN_FLESH)
        normal(368, Items.ENDER_PEARL); normal(369, Items.BLAZE_ROD); normal(370, Items.GHAST_TEAR)
        normal(371, Items.GOLD_NUGGET); normal(372, Items.NETHER_WART); normal(373, Items.POTION)
        normal(374, Items.GLASS_BOTTLE); normal(375, Items.SPIDER_EYE); normal(376, Items.FERMENTED_SPIDER_EYE)
        normal(377, Items.BLAZE_POWDER); normal(378, Items.MAGMA_CREAM); normal(379, Items.BREWING_STAND)
        normal(380, Items.CAULDRON); normal(381, Items.ENDER_EYE); normal(382, Items.GLISTERING_MELON_SLICE)
        // スポーンエッグ (383)
        normal(383, Items.POLAR_BEAR_SPAWN_EGG)
        complex(383, 4, Items.ELDER_GUARDIAN_SPAWN_EGG); complex(383, 5, Items.WITHER_SKELETON_SPAWN_EGG)
        complex(383, 6, Items.STRAY_SPAWN_EGG); complex(383, 23, Items.HUSK_SPAWN_EGG)
        complex(383, 27, Items.ZOMBIE_VILLAGER_SPAWN_EGG); complex(383, 28, Items.SKELETON_HORSE_SPAWN_EGG)
        complex(383, 29, Items.ZOMBIE_HORSE_SPAWN_EGG); complex(383, 31, Items.DONKEY_SPAWN_EGG)
        complex(383, 32, Items.MULE_SPAWN_EGG); complex(383, 35, Items.VEX_SPAWN_EGG)
        complex(383, 36, Items.VINDICATOR_SPAWN_EGG); complex(383, 50, Items.CREEPER_SPAWN_EGG)
        complex(383, 51, Items.SKELETON_SPAWN_EGG); complex(383, 52, Items.SPIDER_SPAWN_EGG)
        complex(383, 54, Items.ZOMBIE_SPAWN_EGG); complex(383, 55, Items.SLIME_SPAWN_EGG)
        complex(383, 56, Items.GHAST_SPAWN_EGG); complex(383, 57, Items.ZOMBIFIED_PIGLIN_SPAWN_EGG)
        complex(383, 58, Items.ENDERMAN_SPAWN_EGG); complex(383, 59, Items.CAVE_SPIDER_SPAWN_EGG)
        complex(383, 60, Items.SILVERFISH_SPAWN_EGG); complex(383, 61, Items.BLAZE_SPAWN_EGG)
        complex(383, 62, Items.MAGMA_CUBE_SPAWN_EGG); complex(383, 63, Items.ENDER_DRAGON_SPAWN_EGG)
        complex(383, 64, Items.WITHER_SPAWN_EGG); complex(383, 65, Items.BAT_SPAWN_EGG)
        complex(383, 66, Items.WITCH_SPAWN_EGG); complex(383, 67, Items.ENDERMITE_SPAWN_EGG)
        complex(383, 68, Items.GUARDIAN_SPAWN_EGG); complex(383, 69, Items.SHULKER_SPAWN_EGG)
        complex(383, 90, Items.PIG_SPAWN_EGG); complex(383, 91, Items.SHEEP_SPAWN_EGG)
        complex(383, 92, Items.COW_SPAWN_EGG); complex(383, 93, Items.CHICKEN_SPAWN_EGG)
        complex(383, 94, Items.SQUID_SPAWN_EGG); complex(383, 95, Items.WOLF_SPAWN_EGG)
        complex(383, 96, Items.MOOSHROOM_SPAWN_EGG); complex(383, 97, Items.SNOW_GOLEM_SPAWN_EGG)
        complex(383, 98, Items.OCELOT_SPAWN_EGG); complex(383, 99, Items.IRON_GOLEM_SPAWN_EGG)
        complex(383, 100, Items.HORSE_SPAWN_EGG); complex(383, 101, Items.RABBIT_SPAWN_EGG)
        complex(383, 102, Items.POLAR_BEAR_SPAWN_EGG); complex(383, 103, Items.LLAMA_SPAWN_EGG)
        complex(383, 105, Items.PARROT_SPAWN_EGG); complex(383, 120, Items.VILLAGER_SPAWN_EGG)
        normal(384, Items.EXPERIENCE_BOTTLE); normal(385, Items.FIRE_CHARGE)
        normal(386, Items.WRITABLE_BOOK); normal(387, Items.WRITTEN_BOOK); normal(388, Items.EMERALD)
        normal(389, Items.ITEM_FRAME); normal(390, Items.FLOWER_POT); normal(391, Items.CARROT)
        normal(392, Items.POTATO); normal(393, Items.BAKED_POTATO); normal(394, Items.POISONOUS_POTATO)
        normal(395, Items.MAP); normal(396, Items.GOLDEN_CARROT)
        complex(397, 0, Items.SKELETON_SKULL); complex(397, 1, Items.WITHER_SKELETON_SKULL)
        complex(397, 2, Items.ZOMBIE_HEAD); complex(397, 3, Items.PLAYER_HEAD)
        complex(397, 4, Items.CREEPER_HEAD); complex(397, 5, Items.DRAGON_HEAD)
        normal(398, Items.CARROT_ON_A_STICK); normal(399, Items.NETHER_STAR)
        normal(400, Items.PUMPKIN_PIE); normal(401, Items.FIREWORK_ROCKET); normal(402, Items.FIREWORK_STAR)
        normal(403, Items.ENCHANTED_BOOK); normal(404, Items.COMPARATOR); normal(405, Items.NETHER_BRICK)
        normal(406, Items.QUARTZ); normal(407, Items.TNT_MINECART); normal(408, Items.HOPPER_MINECART)
        normal(409, Items.PRISMARINE_SHARD); normal(410, Items.PRISMARINE_CRYSTALS)
        normal(411, Items.RABBIT); normal(412, Items.COOKED_RABBIT); normal(413, Items.RABBIT_STEW)
        normal(414, Items.RABBIT_FOOT); normal(415, Items.RABBIT_HIDE); normal(416, Items.ARMOR_STAND)
        normal(417, Items.IRON_HORSE_ARMOR); normal(418, Items.GOLDEN_HORSE_ARMOR)
        normal(419, Items.DIAMOND_HORSE_ARMOR); normal(420, Items.LEAD); normal(421, Items.NAME_TAG)
        normal(422, Items.COMMAND_BLOCK_MINECART); normal(423, Items.MUTTON); normal(424, Items.COOKED_MUTTON)
        // バナー (425) — meta は色番号だが順序が反転している
        complex(425, 15, Items.WHITE_BANNER); complex(425, 14, Items.ORANGE_BANNER)
        complex(425, 13, Items.MAGENTA_BANNER); complex(425, 12, Items.LIGHT_BLUE_BANNER)
        complex(425, 11, Items.YELLOW_BANNER); complex(425, 10, Items.LIME_BANNER)
        complex(425, 9, Items.PINK_BANNER); complex(425, 8, Items.GRAY_BANNER)
        complex(425, 7, Items.LIGHT_GRAY_BANNER); complex(425, 6, Items.CYAN_BANNER)
        complex(425, 5, Items.PURPLE_BANNER); complex(425, 4, Items.BLUE_BANNER)
        complex(425, 3, Items.BROWN_BANNER); complex(425, 2, Items.GREEN_BANNER)
        complex(425, 1, Items.RED_BANNER); complex(425, 0, Items.BLACK_BANNER)
        normal(427, Items.SPRUCE_DOOR); normal(428, Items.BIRCH_DOOR); normal(429, Items.JUNGLE_DOOR)
        normal(430, Items.ACACIA_DOOR); normal(431, Items.DARK_OAK_DOOR)
        // レコード
        normal(2256, Items.MUSIC_DISC_13); normal(2257, Items.MUSIC_DISC_CAT); normal(2258, Items.MUSIC_DISC_BLOCKS)
        normal(2259, Items.MUSIC_DISC_CHIRP); normal(2260, Items.MUSIC_DISC_FAR); normal(2261, Items.MUSIC_DISC_MALL)
        normal(2262, Items.MUSIC_DISC_MELLOHI); normal(2263, Items.MUSIC_DISC_STAL); normal(2264, Items.MUSIC_DISC_STRAD)
        normal(2265, Items.MUSIC_DISC_WARD); normal(2266, Items.MUSIC_DISC_11); normal(2267, Items.MUSIC_DISC_WAIT)
    }

    /** meta 無関係に単一 Item にマップする（Damage をそのままデフォルト値として使用）。 */
    private fun normal(id: Int, item: Item) {
        items[id] = Int2ObjectOpenHashMap<Item>().also { it.defaultReturnValue(item) }
    }

    /** id + meta の組み合わせで Item を登録する。 */
    private fun complex(id: Int, meta: Int, item: Item) {
        items.getOrPut(id) { Int2ObjectOpenHashMap() }.put(meta, item)
    }

    /**
     * NBT タグから id / Damage / Count を読み取り、現代の (Item, count, builder) を解決して返す。
     * 処理済みの "id" / "Damage" / "Count" キーは tag から削除される。
     */
    fun resolve(tag: CompoundTag): Triple<Item, Int, DataComponentPatch.Builder>? {
        val id    = tag.getIntOr("id", 0)
        val meta  = tag.getIntOr("Damage", 0)
        val count = tag.getIntOr("Count", 1)

        val item = items[id]?.get(meta) ?: return null

        tag.remove("id"); tag.remove("Damage"); tag.remove("Count")

        return when {
            item === Items.POTION -> {
                val (potionItem, contents) = LegacyBasePotionItem.resolve(meta, tag.getCompound("tag").getOrNull())
                Triple(potionItem, count, DataComponentPatch.builder().set(DataComponents.POTION_CONTENTS, contents))
            }
            item === Items.FILLED_MAP ->
                Triple(item, count, DataComponentPatch.builder().set(DataComponents.MAP_ID, MapId(meta)))
            item in damageable ->
                Triple(item, count, DataComponentPatch.builder().set(DataComponents.DAMAGE, meta))
            // meta=0 のスポーンエッグ (POLAR_BEAR) はブランクモデルを設定して区別する
            item === Items.POLAR_BEAR_SPAWN_EGG && meta == 0 ->
                Triple(item, count, DataComponentPatch.builder())
            else ->
                Triple(item, count, DataComponentPatch.builder())
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 各 DataComponentFixer 実装用ヘルパー拡張関数（スコープ外）
// ═══════════════════════════════════════════════════════════════════════════════

private fun CompoundTag.getAndRemoveByte(key: String): Byte? {
    val v = this.get(key)?.asByte()?.getOrNull()
    this.remove(key)
    return v
}

private fun CompoundTag.getAndRemoveCompound(key: String): CompoundTag? {
    val v = this.get(key)?.asCompound()?.getOrNull()
    this.remove(key)
    return v
}

private fun CompoundTag.getAndRemoveList(key: String): ListTag? {
    val v = this.get(key)?.asList()?.getOrNull()
    this.remove(key)
    return v
}

private fun CompoundTag.getAndRemoveString(key: String): String? {
    val v = this.get(key)?.asString()?.getOrNull()
    this.remove(key)
    return v
}

private fun CompoundTag.getAndRemoveInt(key: String): Int? {
    val v = this.get(key)?.asInt()?.getOrNull()
    this.remove(key)
    return v
}

private fun CompoundTag.removeIfEmpty(key: String) {
    if (this.getCompoundOrEmpty(key).isEmpty) this.remove(key)
}

// ═══════════════════════════════════════════════════════════════════════════════
// 各 DataComponentFixer 実装
// ═══════════════════════════════════════════════════════════════════════════════

// ── HideFlags → TooltipDisplay ───────────────────────────────────────────────

private object LegacyHideFlagsFixer : DataComponentFixer<TooltipDisplay> {
    override val type = DataComponents.TOOLTIP_DISPLAY
    private val cache = ConcurrentHashMap<Byte, TooltipDisplay>()

    override fun getData(tag: CompoundTag): TooltipDisplay? {
        val flags = tag.getAndRemoveByte("HideFlags")?.toUByte() ?: return null
        return cache.getOrPut(flags.toByte()) {
            val hidden = LinkedHashSet<DataComponentType<*>>()
            if (flags.and(1u)   != 0u.toUByte()) hidden += DataComponents.ENCHANTMENTS
            if (flags.and(2u)   != 0u.toUByte()) hidden += DataComponents.ATTRIBUTE_MODIFIERS
            if (flags.and(4u)   != 0u.toUByte()) hidden += DataComponents.UNBREAKABLE
            if (flags.and(8u)   != 0u.toUByte()) hidden += DataComponents.CAN_BREAK
            if (flags.and(16u)  != 0u.toUByte()) hidden += DataComponents.CAN_PLACE_ON
            if (flags.and(32u)  != 0u.toUByte()) hidden += setOf(
                DataComponents.BANNER_PATTERNS, DataComponents.BEES, DataComponents.BLOCK_ENTITY_DATA,
                DataComponents.BLOCK_STATE, DataComponents.BUNDLE_CONTENTS, DataComponents.CHARGED_PROJECTILES,
                DataComponents.CONTAINER, DataComponents.CONTAINER_LOOT, DataComponents.FIREWORK_EXPLOSION,
                DataComponents.FIREWORKS, DataComponents.INSTRUMENT, DataComponents.MAP_ID,
                DataComponents.PAINTING_VARIANT, DataComponents.POT_DECORATIONS, DataComponents.POTION_CONTENTS,
                DataComponents.TROPICAL_FISH_PATTERN, DataComponents.WRITTEN_BOOK_CONTENT,
                DataComponents.STORED_ENCHANTMENTS,
            )
            if (flags.and(64u)  != 0u.toUByte()) hidden += DataComponents.DYED_COLOR
            if (flags.and(128u) != 0u.toUByte()) hidden += DataComponents.TRIM
            TooltipDisplay(false, hidden)
        }
    }
}

// ── SkullOwner → ResolvableProfile ───────────────────────────────────────────

private object LegacySkullTextureFixer : DataComponentFixer<ResolvableProfile> {
    override val type = DataComponents.PROFILE
    private val cache = ConcurrentHashMap<String, ResolvableProfile>()

    override fun getData(tag: CompoundTag): ResolvableProfile? {
        val skull = tag.getAndRemoveCompound("SkullOwner") ?: return null
        val texture = skull.getCompound("Properties").getOrNull()
            ?.getList("textures")?.getOrNull()
            ?.firstOrNull()?.asCompound()?.getOrNull()
            ?.getString("Value")?.getOrNull() ?: return null

        return cache.getOrPut(texture) {
            val props = PropertyMap(LinkedHashMultimap.create<String, Property>().apply {
                put("textures", Property("textures", texture))
            })
            //? if >= 1.21.9 {
            ResolvableProfile.createResolved(GameProfile(UUID.randomUUID(), "meow", props))
            //?} else {
            /*ResolvableProfile(java.util.Optional.of("meow"), java.util.Optional.of(UUID.randomUUID()), props)*/
            //?}
        }
    }
}

// ── display.Lore → ItemLore ──────────────────────────────────────────────────

private object LegacyLoreFixer : DataComponentFixer<ItemLore> {
    override val type = DataComponents.LORE
    override fun getData(tag: CompoundTag): ItemLore? {
        val display = tag.getCompound("display").getOrNull() ?: return null
        val loreTag = display.getAndRemoveList("Lore") ?: return null
        tag.removeIfEmpty("display")
        val lines = loreTag.mapNotNull { it.asString().getOrNull() }.map { LegacyTextFixer.parse(it) }
        return ItemLore(lines, lines)
    }
}

// ── display.Name → CUSTOM_NAME ───────────────────────────────────────────────

private object LegacyNameFixer : DataComponentFixer<Component> {
    override val type = DataComponents.CUSTOM_NAME
    override fun getData(tag: CompoundTag): Component? {
        val display = tag.getCompound("display").getOrNull() ?: return null
        val name = display.getAndRemoveString("Name") ?: return null
        tag.removeIfEmpty("display")
        return LegacyTextFixer.parse(name)
    }
}

// ── display.color → DYED_COLOR ───────────────────────────────────────────────

private object LegacyColorFixer : DataComponentFixer<DyedItemColor> {
    override val type = DataComponents.DYED_COLOR
    private val cache = ConcurrentHashMap<Int, DyedItemColor>()

    override fun getData(tag: CompoundTag): DyedItemColor? {
        val display = tag.getCompound("display").getOrNull() ?: return null
        val color = display.getAndRemoveInt("color") ?: return null
        tag.removeIfEmpty("display")
        return cache.getOrPut(color) { DyedItemColor(color) }
    }
}

// ── Unbreakable → UNBREAKABLE ────────────────────────────────────────────────

private object LegacyUnbreakableFixer : DataComponentFixer<Unit> {
    override val type = DataComponents.UNBREAKABLE
    override fun getData(tag: CompoundTag): Unit? {
        val v = tag.get("Unbreakable")?.asBoolean()?.getOrNull()
        tag.remove("Unbreakable")
        return if (v == true) Unit.INSTANCE else null
    }
}

// ── ench (旧エンチャントリスト) → ENCHANTMENT_GLINT_OVERRIDE ─────────────────

private object LegacyEnchantGlintFixer : DataComponentFixer<Boolean> {
    override val type = DataComponents.ENCHANTMENT_GLINT_OVERRIDE
    override fun getData(tag: CompoundTag): Boolean? {
        val v = tag.get("ench")?.asList()?.getOrNull()
        tag.remove("ench")
        return if (v != null) true else null
    }
}

// ── WrittenBook メタ → WRITTEN_BOOK_CONTENT ───────────────────────────────────

private object LegacyWrittenBookFixer : DataComponentFixer<WrittenBookContent> {
    override val type = DataComponents.WRITTEN_BOOK_CONTENT
    override fun getData(tag: CompoundTag): WrittenBookContent? {
        val gen = tag.get("generation")?.asInt()?.getOrNull()
        val res = tag.get("resolved")?.asBoolean()?.getOrNull()
        tag.remove("generation"); tag.remove("resolved")
        return if (gen != null || res != null) WrittenBookContent.EMPTY else null
    }
}

// ── BlockEntityTag (Banner) → BANNER_PATTERNS ────────────────────────────────

private val BANNER_PATTERN_MAP = Object2ObjectOpenHashMap<String, Holder<BannerPattern>>().apply {
    fun put(old: String, loc: String) {
        val id = ResourceLocation.parse(loc)
        put(old, Holder.direct(BannerPattern(id, "block.minecraft.banner.${id.toShortLanguageKey()}")))
    }
    put("b","minecraft:base"); put("bl","minecraft:square_bottom_left"); put("br","minecraft:square_bottom_right")
    put("tl","minecraft:square_top_left"); put("tr","minecraft:square_top_right")
    put("bs","minecraft:stripe_bottom"); put("ts","minecraft:stripe_top")
    put("ls","minecraft:stripe_left"); put("rs","minecraft:stripe_right")
    put("cs","minecraft:stripe_center"); put("ms","minecraft:stripe_middle")
    put("drs","minecraft:stripe_downright"); put("dls","minecraft:stripe_downleft")
    put("ss","minecraft:small_stripes"); put("cr","minecraft:cross"); put("sc","minecraft:straight_cross")
    put("bt","minecraft:triangle_bottom"); put("tt","minecraft:triangle_top")
    put("bts","minecraft:triangles_bottom"); put("tts","minecraft:triangles_top")
    put("ld","minecraft:diagonal_left"); put("rd","minecraft:diagonal_up_right")
    put("lud","minecraft:diagonal_up_left"); put("rud","minecraft:diagonal_right")
    put("mc","minecraft:circle"); put("mr","minecraft:rhombus")
    put("vh","minecraft:half_vertical"); put("hh","minecraft:half_horizontal")
    put("vhr","minecraft:half_vertical_right"); put("hhb","minecraft:half_horizontal_bottom")
    put("bo","minecraft:border"); put("cbo","minecraft:curly_border"); put("gra","minecraft:gradient")
    put("gru","minecraft:gradient_up"); put("bri","minecraft:bricks"); put("glb","minecraft:globe")
    put("cre","minecraft:creeper"); put("sku","minecraft:skull"); put("flo","minecraft:flower")
    put("moj","minecraft:mojang"); put("pig","minecraft:piglin")
    val base = ResourceLocation.withDefaultNamespace("base")
    defaultReturnValue(Holder.direct(BannerPattern(base, "block.minecraft.banner.base")))
}

private fun bannerColor(id: Int): DyeColor = when (15 - id) {
    1 -> DyeColor.ORANGE; 2 -> DyeColor.MAGENTA; 3 -> DyeColor.LIGHT_BLUE; 4 -> DyeColor.YELLOW
    5 -> DyeColor.LIME; 6 -> DyeColor.PINK; 7 -> DyeColor.GRAY; 8 -> DyeColor.LIGHT_GRAY
    9 -> DyeColor.CYAN; 10 -> DyeColor.PURPLE; 11 -> DyeColor.BLUE; 12 -> DyeColor.BROWN
    13 -> DyeColor.GREEN; 14 -> DyeColor.RED; 15 -> DyeColor.BLACK; else -> DyeColor.WHITE
}

private object LegacyBannerItemFixer : DataComponentFixer<BannerPatternLayers> {
    override val type = DataComponents.BANNER_PATTERNS
    override fun canApply(item: Item) = item.builtInRegistryHolder().`is`(ItemTags.BANNERS)

    override fun getData(tag: CompoundTag): BannerPatternLayers? {
        val bet = tag.get("BlockEntityTag")?.asCompound()?.getOrNull() ?: return null
        tag.remove("BlockEntityTag")
        val base = bet.get("Base")?.asInt()?.getOrNull() ?: 0
        val listTag = bet.get("Patterns")?.asList()?.getOrNull() ?: return null
        bet.remove("Base"); bet.remove("Patterns")

        val layers = listTag.mapNotNull { it.asCompound().getOrNull() }.mapNotNull { entry ->
            val pattern = entry.getString("Pattern").getOrNull() ?: return@mapNotNull null
            val color   = entry.get("Color")?.asInt()?.getOrNull() ?: return@mapNotNull null
            Layer(BANNER_PATTERN_MAP[pattern]!!, bannerColor(color))
        }.toMutableList()
        layers.addFirst(Layer(BANNER_PATTERN_MAP["b"]!!, bannerColor(base)))
        return BannerPatternLayers(layers)
    }
}

// ── ExtraAttributes → CUSTOM_DATA (SkyBlock 固有データ) ─────────────────────

private object LegacyExtraAttributesFixer : DataComponentFixer<CustomData> {
    override val type = DataComponents.CUSTOM_DATA
    override fun getData(tag: CompoundTag): CustomData? {
        val extra = tag.get("ExtraAttributes")?.asCompound()?.getOrNull() ?: return null
        tag.remove("ExtraAttributes")
        return CustomData.of(extra)
    }
}

// ── Explosion → FIREWORK_EXPLOSION ───────────────────────────────────────────

private object LegacyFireworkExplosionFixer : DataComponentFixer<FireworkExplosion> {
    override val type = DataComponents.FIREWORK_EXPLOSION

    override fun getData(tag: CompoundTag): FireworkExplosion? {
        val exp = tag.get("Explosion")?.asCompound()?.getOrNull() ?: return null
        tag.remove("Explosion")
        val shape      = shapeById(exp.get("Type")?.asInt()?.getOrNull() ?: 0)
        val colors     = exp.get("Colors")?.asIntArray()?.getOrNull().toIntList()
        val fadeColors = exp.get("FadeColors")?.asIntArray()?.getOrNull().toIntList()
        val trail      = exp.get("Trail")?.asBoolean()?.getOrNull() ?: false
        val twinkle    = exp.get("Flicker")?.asBoolean()?.getOrNull() ?: false
        return FireworkExplosion(shape, colors, fadeColors, trail, twinkle)
    }

    private fun IntArray?.toIntList(): IntList = if (this != null) IntArrayList(this) else IntLists.EMPTY_LIST
    private fun shapeById(id: Int) = when (id) {
        1 -> FireworkExplosion.Shape.LARGE_BALL; 2 -> FireworkExplosion.Shape.STAR
        3 -> FireworkExplosion.Shape.CREEPER;    4 -> FireworkExplosion.Shape.BURST
        else -> FireworkExplosion.Shape.SMALL_BALL
    }
}

// ── ItemModel → ITEM_MODEL ────────────────────────────────────────────────────

private object LegacyItemModelFixer : DataComponentFixer<ResourceLocation> {
    override val type = DataComponents.ITEM_MODEL
    private val cache = ConcurrentHashMap<String, ResourceLocation>()

    override fun getData(tag: CompoundTag): ResourceLocation? {
        val raw = tag.get("ItemModel")?.asString()?.getOrNull() ?: return null
        tag.remove("ItemModel")
        return cache.getOrPut(raw) { ResourceLocation.tryParse(raw) ?: return null }
    }
}

// ── 不要なキーを削除するだけの汎用 Fixer ────────────────────────────────────

private class LegacyRemoveFixer(private val key: String) : DataComponentFixer<Unit> {
    // type は呼ばれないが interface 要件のためダミーを設定
    override val type = DataComponents.UNBREAKABLE
    override fun getData(tag: CompoundTag): Unit? { tag.remove(key); return null }
}

// ── CompoundTag ヘルパー（ファイルスコープ） ──────────────────────────────────

private fun CompoundTag.getIntOr(key: String, default: Int): Int =
    this.get(key)?.asInt()?.getOrNull() ?: default

