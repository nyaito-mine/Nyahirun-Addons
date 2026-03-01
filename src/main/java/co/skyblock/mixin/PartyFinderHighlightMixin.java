package co.skyblock.mixin;

import co.skyblock.features.nyahirunaddons.na1.partyfinder.partyFinder;
import co.skyblock.utils.dungeon.ClassState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class PartyFinderHighlightMixin {

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void onDrawSlot(
            GuiGraphics context,
            Slot slot,
            CallbackInfo ci
    ) {

        int EnabledInt = partyFinder.INSTANCE.getPartyFinderConfig();
        if (EnabledInt == 0 || EnabledInt == 1) return;

        Color CanJoin = partyFinder.INSTANCE.getPartyFinderHighlightCanJoinConfig();
        Color CantJoin = partyFinder.INSTANCE.getPartyFinderHighlightCantJoinConfig();

        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        List<Component> tooltip = stack.getTooltipLines(
                Item.TooltipContext.EMPTY,
                player,
                TooltipFlag.NORMAL
        );

        /* ================================
           ① Currently Selected を保存
           ================================ */
        for (Component text : tooltip) {
            String line = text.getString();

            if (line.startsWith("Currently Selected:")) {
                String clazz = line
                        .substring("Currently Selected:".length())
                        .trim();

                ClassState.setSelectedClass(clazz);
            }
        }

        /* ================================
           ② Missing に含まれてたら赤く
           ================================ */

        if (!ClassState.hasSelectedClass()) return;

        String selected = ClassState.getSelectedClass();
        boolean should = false;

        for (Component text : tooltip) {
            String line = text.getString();

            if (line.startsWith("§e§lMissing:") && line.contains(selected)) {
                should = true;
                break;
            }
        }

        boolean hasCanJoin = tooltip.stream()
                .anyMatch(t -> t.getString().contains("Click to join!"));

        boolean hasCantJoin =
                tooltip.stream().anyMatch(t -> t.getString().contains("Dungeon:")) &&
                        tooltip.stream().anyMatch(t -> t.getString().contains("Floor:")) &&
                        tooltip.stream().anyMatch(t -> t.getString().contains("Note:")) &&
                        tooltip.stream().anyMatch(t -> t.getString().contains("Requires "));

        if (should && hasCanJoin) {

            int x = slot.x;
            int y = slot.y;

            context.fill(
                    x,
                    y,
                    x + 16,
                    y + 16,
                    CanJoin.getRGB()
                    //0xA000FF00
            );
        }
        if (hasCantJoin) {

            int x = slot.x;
            int y = slot.y;

            context.fill(
                    x,
                    y,
                    x + 16,
                    y + 16,
                    CantJoin.getRGB()
                    //0xA0FF0000
            );
        }
    }
}
