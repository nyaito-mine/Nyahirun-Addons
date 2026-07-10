package co.stellarskys.nyahirunaddons.mixins;

import co.stellarskys.nyahirunaddons.api.ItemUtils;
import co.stellarskys.nyahirunaddons.features.NonCategory;
import co.stellarskys.nyahirunaddons.features.general.nonCategory.EfficientDB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void nyahirun$effDB(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;

        if (SkyBlockIsland.THE_CATACOMBS.inIsland() && NonCategory.INSTANCE.getEfficientDB()) {
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty() && ItemUtils.INSTANCE.isDungeonBreakerSkyBlock(stack) && !EfficientDB.IGNORED_BLOCKS.contains(state.getBlock())) {
                cir.setReturnValue(1024.0F);
            }
        }
    }
}
