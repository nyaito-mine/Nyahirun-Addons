package co.skyblock.mixin;

import co.skyblock.features.nyahirunaddons.na1.general.efficientDB;
import co.skyblock.utils.dungeon.DBUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class EfficientDBMixin {

    @Inject(
            method = "getDestroySpeed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fixDungeonBreaker(
            BlockState state,
            CallbackInfoReturnable<Float> cir
    ) {
        Player player = (Player) (Object) this;

        boolean Enabled = efficientDB.INSTANCE.getEfficientDBConfig();
        if (!Enabled) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return;

        if (!DBUtils.INSTANCE.isDungeonBreakerSkyBlock(stack)) return;

        if (efficientDB.IGNORED_BLOCKS.contains(state.getBlock())) return;

        cir.setReturnValue(1024.0F);
    }
}
