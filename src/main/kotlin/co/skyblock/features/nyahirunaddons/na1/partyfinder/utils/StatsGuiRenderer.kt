package co.skyblock.features.nyahirunaddons.na1.partyfinder.utils

import co.skyblock.utils.ApiResults
import co.skyblock.utils.ApiResultsProvider
import co.skyblock.utils.dungeon.api.Manager
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

class StatsGuiRenderer(private val playerName: String) : Screen(Component.literal("Stats")) {
    private data class TextRenderEntry(
        val string: String,
        val color: Int,
        val width: Int,
        val isSkillAverage: Boolean = false
    )

    private data class PetRenderEntry(
        val pet: Manager.Pet,
        val texture: net.minecraft.resources.ResourceLocation,
        val tooltip: List<ClientTooltipComponent>
    )

    private var cachedApis = ApiResultsProvider.getAPIResults(playerName)
    private var cachedTexts: List<TextRenderEntry> = emptyList()
    private var cachedSkillTooltip: List<ClientTooltipComponent> = emptyList()
    private var cachedPetEntries: List<PetRenderEntry> = emptyList()
    private var cachedItems: List<ItemStack> = emptyList()
    private var cachedWardrobeDisplay: List<ItemStack> = List(36) { ItemStack.EMPTY }
    private var cachedEquipments: List<ItemStack> = emptyList()
    private val itemTooltipCache = mutableMapOf<Int, List<ClientTooltipComponent>>()
    private val wardrobeTooltipCache = mutableMapOf<Int, List<ClientTooltipComponent>>()
    private val equipmentTooltipCache = mutableMapOf<Int, List<ClientTooltipComponent>>()

    override fun init() {
        super.init()
        rebuildCachedState()
    }


    override fun isPauseScreen(): Boolean = false

    override fun render(gui: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (cachedTexts.isEmpty()) {
            rebuildCachedState()
        }

        var tooltip: List<ClientTooltipComponent>? = null
        super.render(gui, mouseX, mouseY, partialTick)

        gui.fill(0, 0, width, height, 0x88000000.toInt())

        val panelWidth = 400
        val panelHeight = 280

        val x = width / 2 - panelWidth / 2
        val y = height / 2 - panelHeight / 2

        val background = 0xFFC6C6C6.toInt()
        val dark = 0xFF555555.toInt()
        val light = 0xFFFFFFFF.toInt()

        gui.fill(x, y, x + panelWidth, y + panelHeight, background)

        gui.fill(x, y, x + panelWidth, y + 2, light)
        gui.fill(x, y, x + 2, y + panelHeight, light)
        gui.fill(x + panelWidth - 2, y, x + panelWidth, y + panelHeight, dark)
        gui.fill(x, y + panelHeight - 2, x + panelWidth, y + panelHeight, dark)
        gui.fill(x + panelWidth - 2, y, x + panelWidth, y + 2, background)
        gui.fill(x, y + panelHeight - 2, x + 2, y + panelHeight, background)

        val scale = 1.3f
        val nameText = "$playerName Stats"
        val textWidth = font.width(nameText)
        gui.pose().pushMatrix()
        gui.pose().scale(scale, scale)
        gui.drawString(
            font,
            nameText,
            ((x + 6) / scale).toInt(),
            ((y + 6) / scale).toInt(),
            0xFF404040.toInt(),
            false
        )
        gui.pose().popMatrix()
        gui.fill(
            x + 6,
            y + 6 + 9,
            x + 6 + (textWidth * scale).toInt(),
            y + 6 + 10,
            0xFF404040.toInt()
        )

        val startX = x + 20
        val startY = y + 30
        var widthText = 0
        var heightText = 0
        val textScale = 1.2f
        gui.pose().pushMatrix()
        gui.pose().scale(textScale, textScale)
        for (showText in cachedTexts) {
            if (showText.string == "\n") {
                heightText += (10 * textScale).toInt()
                widthText = 0
                continue
            } else if (showText.string.isEmpty()) {
                heightText += (20 * textScale).toInt()
                widthText = 0
                continue
            }

            val textW = (showText.width * textScale).toInt()
            val textH = (9 * textScale).toInt()

            gui.drawString(
                font,
                showText.string,
                ((startX + widthText) / textScale).toInt(),
                ((startY + heightText) / textScale).toInt(),
                showText.color,
                false
            )

            if (showText.isSkillAverage) {
                if (mouseX >= startX + widthText && mouseX <= startX + widthText + textW &&
                    mouseY >= startY + heightText && mouseY <= startY + heightText + textH
                ) {
                    tooltip = cachedSkillTooltip
                }
            }

            widthText += (showText.width * textScale).toInt()
        }
        gui.pose().popMatrix()

        val imageSize = 20
        val imgX = startX + 200
        var imgY = startY
        val petRenderSize = 16
        val petRenderOffset = (imageSize - petRenderSize) / 2
        val resourceScale = petRenderSize / 185f
        val slotBackground = 0xFF8B8B8B.toInt()

        gui.pose().pushMatrix()
        gui.pose().scale(textScale, textScale)
        gui.drawString(
            font,
            "§lPets",
            (imgX / textScale).toInt(),
            (imgY / textScale).toInt(),
            0xFFCC40FF.toInt(),
            false
        )
        gui.pose().popMatrix()

        var petCount = 0
        var currentY = 0
        var currentX = imgX
        imgY += 12

        for (petEntry in cachedPetEntries) {
            petCount += 1

            if (petCount == 7) {
                currentX = imgX
                currentY += 22
            }
            if (petCount == 13) {
                currentX = imgX
                currentY += 22
            }

            gui.fill(currentX, imgY + currentY, currentX + imageSize, imgY + currentY + imageSize, slotBackground)

            gui.fill(currentX, imgY + currentY, currentX + imageSize, imgY + currentY + 1, light)
            gui.fill(currentX, imgY + currentY, currentX + 1, imgY + currentY + imageSize, light)
            gui.fill(currentX + imageSize - 1, imgY + currentY, currentX + imageSize, imgY + currentY + imageSize, dark)
            gui.fill(currentX, imgY + currentY + imageSize - 1, currentX + imageSize, imgY + currentY + imageSize, dark)
            gui.fill(
                currentX + imageSize - 1,
                imgY + currentY,
                currentX + imageSize,
                imgY + currentY + 1,
                slotBackground
            )
            gui.fill(
                currentX,
                imgY + currentY + imageSize - 1,
                currentX + 1,
                imgY + currentY + imageSize,
                slotBackground
            )

            gui.pose().pushMatrix()
            gui.pose().translate((currentX + petRenderOffset).toFloat(), (imgY + currentY + petRenderOffset).toFloat())
            gui.pose().scale(resourceScale, resourceScale)

            gui.blit(
                RenderPipelines.GUI_TEXTURED,
                petEntry.texture,
                0,
                0,
                0f,
                0f,
                185,
                185,
                185,
                185
            )

            gui.pose().popMatrix()

            if (mouseX >= currentX && mouseX <= currentX + imageSize &&
                mouseY >= imgY + currentY && mouseY <= imgY + currentY + imageSize
            ) {
                tooltip = petEntry.tooltip
            }

            currentX += imageSize + 4
        }

        var imgYItem = startY + 80
        gui.pose().pushMatrix()
        gui.pose().scale(textScale, textScale)
        gui.drawString(
            font,
            "§lItems",
            (imgX / textScale).toInt(),
            (imgYItem / textScale).toInt(),
            0xFFCC40FF.toInt(),
            false
        )
        gui.pose().popMatrix()

        var itemCount = 0
        var currentYItem = 0
        var currentXItem = imgX
        val itemRenderOffset = (imageSize - 16) / 2
        imgYItem += 12

        for ((itemIndex, item) in cachedItems.withIndex()) {
            if (item.isEmpty) continue

            itemCount += 1

            if (itemCount == 7) {
                currentXItem = imgX
                currentYItem += 22
            }
            if (itemCount == 13) {
                currentXItem = imgX
                currentYItem += 22
            }

            gui.fill(
                currentXItem,
                imgYItem + currentYItem,
                currentXItem + imageSize,
                imgYItem + currentYItem + imageSize,
                slotBackground
            )

            gui.fill(
                currentXItem,
                imgYItem + currentYItem,
                currentXItem + imageSize,
                imgYItem + currentYItem + 1,
                light
            )
            gui.fill(
                currentXItem,
                imgYItem + currentYItem,
                currentXItem + 1,
                imgYItem + currentYItem + imageSize,
                light
            )
            gui.fill(
                currentXItem + imageSize - 1,
                imgYItem + currentYItem,
                currentXItem + imageSize,
                imgYItem + currentYItem + imageSize,
                dark
            )
            gui.fill(
                currentXItem,
                imgYItem + currentYItem + imageSize - 1,
                currentXItem + imageSize,
                imgYItem + currentYItem + imageSize,
                dark
            )
            gui.fill(
                currentXItem + imageSize - 1,
                imgYItem + currentYItem,
                currentXItem + imageSize,
                imgYItem + currentYItem + 1,
                slotBackground
            )
            gui.fill(
                currentXItem,
                imgYItem + currentYItem + imageSize - 1,
                currentXItem + 1,
                imgYItem + currentYItem + imageSize,
                slotBackground
            )

            gui.renderItem(item, currentXItem + itemRenderOffset, imgYItem + currentYItem + itemRenderOffset)
            gui.renderItemDecorations(
                font,
                item,
                currentXItem + itemRenderOffset,
                imgYItem + currentYItem + itemRenderOffset
            )

            if (mouseX >= currentXItem && mouseX <= currentXItem + imageSize &&
                mouseY >= imgYItem + currentYItem && mouseY <= imgYItem + currentYItem + imageSize
            ) {
                minecraft?.let { client ->
                    tooltip = itemTooltipCache.getOrPut(itemIndex) {
                        getTooltipFromItem(client, item).map {
                            ClientTooltipComponent.create(it.visualOrderText)
                        }
                    }
                }
            }

            currentXItem += imageSize + 4
        }

        var imgYWardrobe = imgYItem + 50
        gui.pose().pushMatrix()
        gui.pose().scale(textScale, textScale)
        gui.drawString(
            font,
            "§lWardrobe, Equipment",
            (imgX / textScale).toInt(),
            (imgYWardrobe / textScale).toInt(),
            0xFFCC40FF.toInt(),
            false
        )
        gui.pose().popMatrix()

        val wardrobeImageSize = 16
        val wardrobeSlotStepX = wardrobeImageSize + 2
        val wardrobeSlotStepY = wardrobeImageSize + 1
        imgYWardrobe += 12

        val wardrobeSlotsPerRow = 9
        val wardrobeRows = 4
        val wardrobeDisplaySlots = wardrobeSlotsPerRow * wardrobeRows
        repeat(wardrobeDisplaySlots) { index ->
            val displayItem = cachedWardrobeDisplay.getOrElse(index) { ItemStack.EMPTY }
            val currentXWardrobe = imgX + (index % wardrobeSlotsPerRow) * wardrobeSlotStepX
            val currentYWardrobe = (index / wardrobeSlotsPerRow) * wardrobeSlotStepY

            // 空スロットも枠だけ描画
            gui.fill(
                currentXWardrobe,
                imgYWardrobe + currentYWardrobe,
                currentXWardrobe + wardrobeImageSize,
                imgYWardrobe + currentYWardrobe + wardrobeImageSize,
                slotBackground
            )
            gui.fill(
                currentXWardrobe,
                imgYWardrobe + currentYWardrobe,
                currentXWardrobe + wardrobeImageSize,
                imgYWardrobe + currentYWardrobe + 1,
                light
            )
            gui.fill(
                currentXWardrobe,
                imgYWardrobe + currentYWardrobe,
                currentXWardrobe + 1,
                imgYWardrobe + currentYWardrobe + wardrobeImageSize,
                light
            )
            gui.fill(
                currentXWardrobe + wardrobeImageSize - 1,
                imgYWardrobe + currentYWardrobe,
                currentXWardrobe + wardrobeImageSize,
                imgYWardrobe + currentYWardrobe + wardrobeImageSize,
                dark
            )
            gui.fill(
                currentXWardrobe,
                imgYWardrobe + currentYWardrobe + wardrobeImageSize - 1,
                currentXWardrobe + wardrobeImageSize,
                imgYWardrobe + currentYWardrobe + wardrobeImageSize,
                dark
            )
            gui.fill(
                currentXWardrobe + wardrobeImageSize - 1,
                imgYWardrobe + currentYWardrobe,
                currentXWardrobe + wardrobeImageSize,
                imgYWardrobe + currentYWardrobe + 1,
                slotBackground
            )
            gui.fill(
                currentXWardrobe,
                imgYWardrobe + currentYWardrobe + wardrobeImageSize - 1,
                currentXWardrobe + 1,
                imgYWardrobe + currentYWardrobe + wardrobeImageSize,
                slotBackground
            )

            if (!displayItem.isEmpty) {
                gui.renderItem(displayItem, currentXWardrobe, imgYWardrobe + currentYWardrobe)
                gui.renderItemDecorations(font, displayItem, currentXWardrobe, imgYWardrobe + currentYWardrobe)

                if (mouseX >= currentXWardrobe && mouseX <= currentXWardrobe + wardrobeImageSize &&
                    mouseY >= imgYWardrobe + currentYWardrobe && mouseY <= imgYWardrobe + currentYWardrobe + wardrobeImageSize
                ) {
                    minecraft?.let { client ->
                        tooltip = wardrobeTooltipCache.getOrPut(index) {
                            getTooltipFromItem(client, displayItem).map {
                                ClientTooltipComponent.create(it.visualOrderText)
                            }
                        }
                    }
                }
            }
        }

        val imgYEquipment = imgYWardrobe + 72
        var currentXEquipment = imgX

        for ((equipmentIndex, equipment) in cachedEquipments.withIndex()) {
            if (equipment.isEmpty) continue

            gui.fill(
                currentXEquipment, imgYEquipment,
                currentXEquipment + wardrobeImageSize, imgYEquipment + wardrobeImageSize, slotBackground
            )

            gui.fill(currentXEquipment, imgYEquipment, currentXEquipment + wardrobeImageSize, imgYEquipment + 1, light)
            gui.fill(currentXEquipment, imgYEquipment, currentXEquipment + 1, imgYEquipment + wardrobeImageSize, light)
            gui.fill(
                currentXEquipment + wardrobeImageSize - 1,
                imgYEquipment,
                currentXEquipment + wardrobeImageSize,
                imgYEquipment + wardrobeImageSize,
                dark
            )
            gui.fill(
                currentXEquipment,
                imgYEquipment + wardrobeImageSize - 1,
                currentXEquipment + wardrobeImageSize,
                imgYEquipment + wardrobeImageSize,
                dark
            )
            gui.fill(
                currentXEquipment + wardrobeImageSize - 1,
                imgYEquipment,
                currentXEquipment + wardrobeImageSize,
                imgYEquipment + 1,
                slotBackground
            )
            gui.fill(
                currentXEquipment,
                imgYEquipment + wardrobeImageSize - 1,
                currentXEquipment + 1,
                imgYEquipment + wardrobeImageSize,
                slotBackground
            )

            gui.renderItem(equipment, currentXEquipment, imgYEquipment)
            gui.renderItemDecorations(font, equipment, currentXEquipment, imgYEquipment)

            if (mouseX >= currentXEquipment && mouseX <= currentXEquipment + wardrobeImageSize &&
                mouseY >= imgYEquipment && mouseY <= imgYEquipment + wardrobeImageSize
            ) {
                minecraft?.let { client ->
                    tooltip = equipmentTooltipCache.getOrPut(equipmentIndex) {
                        getTooltipFromItem(client, equipment).map {
                            ClientTooltipComponent.create(it.visualOrderText)
                        }
                    }
                }
            }

            currentXEquipment += wardrobeImageSize + 2
        }

        tooltip?.let {
            gui.renderTooltip(
                font,
                it,
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
            )
        }
    }

    private fun rebuildCachedState() {
        val updatedApis = ApiResultsProvider.getAPIResults(playerName)
        if (updatedApis === cachedApis && cachedTexts.isNotEmpty()) return

        cachedApis = updatedApis
        cachedTexts = TextLines.getGUIText(
            updatedApis.sbLevel,
            updatedApis.magicalPower,
            updatedApis.skillAverage,
            updatedApis.cataLevel,
            updatedApis.archerLevel,
            updatedApis.berserkLevel,
            updatedApis.mageLevel,
            updatedApis.tankLevel,
            updatedApis.healerLevel,
            updatedApis.secrets,
            updatedApis.secretAverage,
            updatedApis.pbF7,
            updatedApis.pbM4,
            updatedApis.pbM5,
            updatedApis.pbM6,
            updatedApis.pbM7
        ).map {
            TextRenderEntry(
                it.string,
                it.color,
                font.width(it.string),
                it.string.contains("§nSkill Average")
            )
        }
        cachedSkillTooltip = buildSkillTooltip(updatedApis)
        cachedPetEntries = updatedApis.pets.mapNotNull { buildPetEntry(it, updatedApis.goldCollection) }
        cachedItems = updatedApis.items.filterNot { it.isEmpty }
        cachedWardrobeDisplay = buildWardrobeDisplayItems(updatedApis)
        cachedEquipments = updatedApis.equipments.filterNot { it.isEmpty }
        itemTooltipCache.clear()
        wardrobeTooltipCache.clear()
        equipmentTooltipCache.clear()
    }

    private fun buildSkillTooltip(apis: ApiResults): List<ClientTooltipComponent> = listOf(
        tooltipLine("- §bSkills§r -"),
        tooltipLine("> §eCombat §6${apis.combatLevel}"),
        tooltipLine("> §eFarming §6${apis.farmingLevel}"),
        tooltipLine("> §eFishing §6${apis.fishingLevel}"),
        tooltipLine("> §eMining §6${apis.miningLevel}"),
        tooltipLine("> §eForaging §6${apis.foragingLevel}"),
        tooltipLine("> §eEnchanting §6${apis.enchantingLevel}"),
        tooltipLine("> §eAlchemy §6${apis.alchemyLevel}"),
        tooltipLine("> §eCarpentry §6${apis.carpentryLevel}"),
        tooltipLine("> §eTaming §6${apis.tamingLevel}"),
        tooltipLine("> §eHunting §6${apis.huntingLevel}")
    )

    private fun buildPetEntry(pet: Manager.Pet, goldCollection: String?): PetRenderEntry? {
        val texture = when (pet.name) {
            "Baby Yeti" -> ResourceLocation.BABY_YETI
            "Black Cat" -> ResourceLocation.BLACK_CAT_PET
            "Ender Dragon" -> ResourceLocation.ENDER_DRAGON_PET
            "Golden Dragon" -> ResourceLocation.GOLDEN_DRAGON_PET
            "Jellyfish" -> ResourceLocation.JELLYFISH
            "Phoenix" -> ResourceLocation.PHOENIX
            "Spirit" -> ResourceLocation.SPIRIT
            else -> return null
        }

        val rarityColor = when (pet.rarity) {
            "MYTHIC" -> "§d"
            "LEGENDARY" -> "§6"
            "EPIC" -> "§5"
            "RARE" -> "§9"
            "UNCOMMON" -> "§a"
            "COMMON" -> "§f"
            else -> ""
        }

        val tooltip = mutableListOf(
            tooltipLine("§7[Lvl ${pet.level}] ${rarityColor}${pet.name}"),
            tooltipLine("§6Held Item: §b${pet.item}")
        )
        if (pet.name == "Golden Dragon") {
            tooltip += tooltipLine("§aGold Collection: §b$goldCollection")
        }

        return PetRenderEntry(pet, texture, tooltip)
    }

    private fun buildWardrobeDisplayItems(apis: ApiResults): List<ItemStack> {
        val wardrobeSlotsPerRow = 9
        val wardrobeRows = 4
        val displayItems = MutableList(wardrobeSlotsPerRow * wardrobeRows) { index ->
            apis.wardrobe.getOrNull(index) ?: ItemStack.EMPTY
        }

        val equippedWardrobeSlot = apis.wardrobeEquipped
        if (equippedWardrobeSlot == null || equippedWardrobeSlot !in 1..wardrobeSlotsPerRow) {
            return displayItems
        }

        val armorFallbacks = listOf(
            findArmorFallback(apis.armor) { path -> path.endsWith("_helmet") || path.endsWith("player_head") },
            findArmorFallback(apis.armor) { path -> path.endsWith("_chestplate") },
            findArmorFallback(apis.armor) { path -> path.endsWith("_leggings") },
            findArmorFallback(apis.armor) { path -> path.endsWith("_boots") }
        )

        repeat(wardrobeRows) { row ->
            val index = row * wardrobeSlotsPerRow + (equippedWardrobeSlot - 1)
            if (displayItems[index].isEmpty && !armorFallbacks[row].isEmpty) {
                displayItems[index] = armorFallbacks[row]
            }
        }

        return displayItems
    }

    private fun findArmorFallback(
        armorItems: List<ItemStack>,
        matcher: (String) -> Boolean
    ): ItemStack = armorItems.firstOrNull { armorItem ->
        matcher(BuiltInRegistries.ITEM.getKey(armorItem.item).path)
    } ?: ItemStack.EMPTY

    private fun tooltipLine(text: String): ClientTooltipComponent =
        ClientTooltipComponent.create(Component.literal(text).visualOrderText)
}