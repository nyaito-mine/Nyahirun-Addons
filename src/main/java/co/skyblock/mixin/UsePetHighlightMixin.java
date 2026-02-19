package co.skyblock.mixin;

import co.skyblock.features.nyahirunaddons.na1.general.usePetHighlight;
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
public abstract class UsePetHighlightMixin {

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void onDrawSlot(
            GuiGraphics context,
            Slot slot,
            CallbackInfo ci
    ) {

        boolean Enabled = usePetHighlight.INSTANCE.getUsePetHighlightConfig();
        if (!Enabled) return;

        Color HighlightColor = usePetHighlight.INSTANCE.getUsePetHighlightColorConfig();

        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        List<Component> tooltip = stack.getTooltipLines(
                Item.TooltipContext.EMPTY,
                player,
                TooltipFlag.NORMAL
        );

        boolean UsePet = tooltip.stream()
                .anyMatch(t -> t.getString().contains("Click to despawn!"));

        if (UsePet) {

            int x = slot.x;
            int y = slot.y;

            context.fill(
                    x,
                    y,
                    x + 16,
                    y + 16,
                    HighlightColor.getRGB()
            );
        }
    }
}
