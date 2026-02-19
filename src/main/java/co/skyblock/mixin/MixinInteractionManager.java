/*
package co.skyblock.mixin;

import co.stellarskys.stella.events.EventBus;
import co.skyblock.events.core.InteractionEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinInteractionManager {

    // ===== ブロック設置 =====
    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onUseItemOn(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof BlockItem) {
            InteractionEvent.PlaceAttempt event =
                    new InteractionEvent.PlaceAttempt(
                            player,
                            hand,
                            stack,
                            hitResult
                    );

            if (EventBus.INSTANCE.post(event)) {
                cir.setReturnValue(event.getResult());

            }
        }

        InteractionEvent.ItemUseAttempt event =
                new InteractionEvent.ItemUseAttempt(
                        player,
                        hand,
                        stack
                );

        if (EventBus.INSTANCE.post(event)) {
            cir.setReturnValue(event.getResult());
        }
    }

    // ===== アイテム使用 =====
    @Inject(
            method = "useItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onUseItem(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        ItemStack stack = player.getItemInHand(hand);

        InteractionEvent.ItemUseAttempt event =
                new InteractionEvent.ItemUseAttempt(
                        player,
                        hand,
                        stack
                );

        if (EventBus.INSTANCE.post(event)) {
            cir.setReturnValue(event.getResult());
        }
    }
}
*/
