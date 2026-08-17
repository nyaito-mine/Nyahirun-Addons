package co.stellarskys.nyahirunaddons.mixins;

import co.stellarskys.nyahirunaddons.features.PartyFinder;
import co.stellarskys.nyahirunaddons.features.dungeon.partyFinder.Info;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/item/ItemStack;III)V"))
    private void nyahirun$drawOnItem(CallbackInfo ci, @Local(argsOnly = true, name = "graphics") GuiGraphicsExtractor graphics, @Local(argsOnly = true, name = "slot") Slot slot) {
        //PartyFinder Highlight
        if (PartyFinder.INSTANCE.getInfo() == 2 || PartyFinder.INSTANCE.getInfo() == 3) {
            List<Component> tooltip = slot.getItem().getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);

            String selected = Info.INSTANCE.getClass();
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

            if (should && hasCanJoin) graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, PartyFinder.INSTANCE.getHighlightCanJoin().getRGB());
            if (hasCantJoin) graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, PartyFinder.INSTANCE.getHighlightCantJoin().getRGB());
        }
    }
}
