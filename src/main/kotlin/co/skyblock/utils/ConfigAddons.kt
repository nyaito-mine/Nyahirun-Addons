package co.skyblock.utils

import co.stellarskys.stella.utils.config
import java.awt.Color

object ConfigAddons { // オブジェクトにまとめる
    init {
        config.category("NA1") {

            subcategory("General") {
                dropdown {
                    configName = "partyFinder"
                    name = "Party Finder"
                    description = "More Info and Highlight"
                    options = listOf("-", "MoreInfo", "Highlight", "Both")
                    default = 0
                }

                colorpicker {
                    configName = "partyFinderHighlightCanJoin"
                    name = "Can Join"
                    description = "ColorPicker Highlight Can Join"
                    default = Color(0, 255, 0, 180)
                }

                colorpicker {
                    configName = "partyFinderHighlightCantJoin"
                    name = "Cant Join"
                    description = "ColorPicker Highlight Cant Join"
                    default = Color(255, 0, 0, 180)
                }

                toggle {
                    configName = "usePetHighlight"
                    name = "Use Pet Highlight"
                    description = "Use Pet Highlight"
                    default = false
                }

                colorpicker {
                    configName = "usePetHighlightColor"
                    name = "Pet Color"
                    description = "ColorPicker Highlight Use Pet"
                    default = Color(0, 255, 0, 180)
                }

                toggle {
                    configName = "efficientDB"
                    name = "⚠ Efficient DB"
                    description = "This Setting Dangerous"
                    default = false
                }
            }

            subcategory("AutoRefill", "autoRefill", "Enables AutoRefill in dungeon") {
                toggle {
                    configName = "autoRefillEnderPearl"
                    name = "Ender Pearl"
                    description = "AutoRefill Ender Pearl(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "autoRefillSuperboomTNT"
                    name = "SuperboomTNT"
                    description = "AutoRefill SuperboomTNT(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "autoRefillSpiritLeap"
                    name = "Spirit Leap"
                    description = "AutoRefill Spirit Leap(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "autoRefillInflatableJerry"
                    name = "Inflatable Jerry"
                    description = "AutoRefill Inflatable Jerry(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "autoRefillDecoy"
                    name = "Decoy"
                    description = "AutoRefill Decoy(In Dungeon)"
                    default = false
                }
            }

            subcategory("Render Highlight", "renderHighlight", "Enables Render Highlight in dungeon") {
                toggle {
                    configName = "renderHighlightDropItem"
                    name = "Drop Item(Secret)"
                    description = "Render Highlight Drop Item(In Dungeon)"
                    default = false
                }

                colorpicker {
                    configName = "renderHighlightDropItemColor"
                    name = "Drop Item Color"
                    description = "ColorPicker Render Highlight Drop Item"
                    default = Color(0, 255, 0, 255)
                }

                toggle {
                    configName = "renderHighlightWither"
                    name = "Wither"
                    description = "Render Highlight Wither(In Dungeon)"
                    default = false
                }

                colorpicker {
                    configName = "renderHighlightWitherColorFace"
                    name = "Wither Color Face"
                    description = "ColorPicker Render Highlight Wither Face"
                    default = Color(0, 255, 255, 153)
                }

                colorpicker {
                    configName = "renderHighlightWitherColorLine"
                    name = "Wither Color Line"
                    description = "ColorPicker Render Highlight Wither Line"
                    default = Color(0, 255, 255, 255)
                }

                toggle {
                    configName = "renderHighlightMimicChest"
                    name = "Mimic Chest"
                    description = "Render Highlight Mimic Chest(In Dungeon)"
                    default = false
                }

                colorpicker {
                    configName = "renderHighlightMimicChestColorFace"
                    name = "Mimic Chest Color Face"
                    description = "ColorPicker Render Highlight Wither Face"
                    default = Color(255, 0, 0, 153)
                }

                colorpicker {
                    configName = "renderHighlightMimicChestColorLine"
                    name = "Mimic Chest Color Line"
                    description = "ColorPicker Render Highlight Mimic Chest Line"
                    default = Color(255, 0, 0, 255)
                }
            }

            subcategory("Disable Use", "disableUse", "Enables Disable Use in dungeon") {
                toggle {
                    configName = "disableUseSecondSoulSand"
                    name = "Second SoulSand"
                    description = "Disable Use Second SoulSand(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "disableUsePlaceTuba"
                    name = "Place Tuba"
                    description = "Disable Use Place Tuba(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "disableUseSBMenu"
                    name = "SBMenu"
                    description = "Disable Use SBMenu(In Dungeon)"
                    default = false
                }
            }
        }

        config.category("NA2") {

            subcategory("Notification", "notification", "Enables Notification in dungeon") {
                toggle {
                    configName = "notificationEnraged"
                    name = "Enraged Wish"
                    description = "Notification Enraged(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "notificationGate"
                    name = "Gate Breaked"
                    description = "Notification Gate(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "notificationCoreLeap"
                    name = "Core Leap"
                    description = "Notification CoreLeap(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "notificationNecronLeap"
                    name = "Necron Leap"
                    description = "Notification NecronLeap(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "notificationRagnarock"
                    name = "Ragnarock"
                    description = "Notification Ragnarock(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "notificationChestLog"
                    name = "ChestLog"
                    description = "Notification ChestLog(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "notificationMask"
                    name = "Mask"
                    description = "Notification Mask(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "notificationKeyPick"
                    name = "Key Pick"
                    description = "Notification KeyPick(In Dungeon)"
                    default = false
                }
            }

            subcategory("Chat Hider", "chatHider", "Enables Chat Hider in dungeon") {
                toggle {
                    configName = "chathiderObtained"
                    name = "Obtained"
                    description = "Hide Chat Obtained(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderMilestone"
                    name = "Milestone"
                    description = "Hide Chat Milestone(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderKillCombo"
                    name = "KillCombo"
                    description = "Hide Chat KillCombo(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderBoss"
                    name = "Boss"
                    description = "Hide Chat Boss(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderNPCMort"
                    name = "NPCMort"
                    description = "Hide Chat NPCMort(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderTeleportCooldown"
                    name = "TeleportCooldown"
                    description = "Hide Chat TeleportCooldown(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderImplosion"
                    name = "Implosion"
                    description = "Hide Chat Implosion(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderTrapRoom"
                    name = "TrapRoom"
                    description = "Hide Chat TrapRoom(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderLever"
                    name = "Lever"
                    description = "Hide Chat Lever(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderChest"
                    name = "Chest"
                    description = "Hide Chat Chest(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderIcePath"
                    name = "IcePath"
                    description = "Hide Chat IcePath(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderMysticalForce"
                    name = "MysticalForce"
                    description = "Hide Chat MysticalForce(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderLostAdventure"
                    name = "LostAdventure"
                    description = "Hide Chat LostAdventure(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderEssence"
                    name = "Essence"
                    description = "Hide Chat Essence(In Dungeon)"
                    default = false
                }

                toggle {
                    configName = "chathiderBlessing"
                    name = "Blessing"
                    description = "Hide Chat Blessing(In Dungeon)"
                    default = false
                }
            }

            subcategory("Test") {
                toggle {
                    configName = "testConfig"
                    name = "Test"
                    description = "This is Description"
                    default = false
                }

                slider {
                    configName = "sliderTest"
                    name = "Slider"
                    description = "Slider Description"
                    min = 1f
                    max = 10f
                    default = 5f
                }

                colorpicker {
                    configName = "colorPickerTest"
                    name = "ColorPicker"
                    description = "ColorPicker Description"
                    default = Color(0, 255, 255, 255)
                }
            }
        }
    }
}
