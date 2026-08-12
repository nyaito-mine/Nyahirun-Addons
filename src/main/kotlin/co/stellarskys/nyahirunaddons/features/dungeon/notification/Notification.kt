package co.stellarskys.nyahirunaddons.features.dungeon.notification

import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.zenith.player
import co.stellarskys.nyahirunaddons.features.Notification as Noti
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.nyahirunaddons.api.render.screen.GuiUtils
import co.stellarskys.stella.events.core.GuiEvent
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.hud.HUDManager
import co.stellarskys.stella.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object Notification : Feature("Notification") {
    var renderTitle: String = ""
    var renderTicks: Int = 0

    private val notificationConfigs by lazy { listOf(
        Triple(Noti.EnragedWish, "⚠ Maxor is enraged! ⚠", "§l§cWish"),
        Triple(Noti.GateBroke, "The gate has been destroyed!", "§l§cGate Breaked"),
        Triple(Noti.CoreLeap, "The Core entrance is opening!", "§l§cLeap"),
        Triple(Noti.NecronLeap, "Goodbye.", "§l§cLeap"),
        Triple(Noti.Ragnarock, "I no longer wish to fight, but I know that will not stop you.", "§l§cRagnarock"),
        Triple(Noti.ChestLock, "That chest is locked!", "§l§cLocked"),
        Triple(Noti.Mask, "Bonzo's Mask saved your life!", "§l§bBonzo's Mask"),
        Triple(Noti.Mask, "Second Wind Activated! Your Spirit Mask saved your life!", "§l§bSpirit Mask"),
        Triple(Noti.Mask, "Your Phoenix Pet saved you from certain death!", "§l§bPhoenix"),
        Triple(Noti.KeyPick, "has obtained Wither Key!", "§l§eWither Key Pick"),
        Triple(Noti.KeyPick, "has obtained Blood Key!", "§l§eBlood Key Pick")
    ) }

    override fun initialize() {
        HUDManager.register("notification", "This is Notification", "Notification")

        on<GuiEvent.RenderHUD> { renderHUD(it.context) }

        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            for (entry in notificationConfigs) {
                val enabled = entry.first
                val trigger = entry.second
                val title = entry.third

                if (enabled && msg.contains(trigger)) {
                    renderTitle = title
                    renderTicks = 30

                    player?.playSound(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0f,
                        0.5f
                    )
                }
            }
        }

        on<TickEvent.Client> {
            if (renderTicks > 0) {
                renderTicks--
            } else {
                renderTitle = ""
                renderTicks = 0
            }
        }
    }

    private fun renderHUD(context: GuiGraphicsExtractor) = HUDManager.renderHud("notification", context) {
        val contentWidth = GuiUtils.getWidth("This is Notification")
        val contentHeight = GuiUtils.getHeight()
        val scale = 1.1f
        val x = (((contentWidth - GuiUtils.getWidth(renderTitle) * scale) / 2) / scale).toInt()
        val y = (((contentHeight - GuiUtils.getHeight() * scale) / 2) / scale).toInt() + 1 //なぜか1pxずらすと見た目がよくなる
        Render2D.drawString(context, renderTitle, x, y, scale, true)
    }
}