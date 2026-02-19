package co.skyblock.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import co.skyblock.utils.render.EventManager;
import co.skyblock.utils.render.RenderEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(at = @At("RETURN"), method = "renderLevel")
    void render(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        PoseStack matrixStack = new PoseStack();
        matrixStack.mulPose(positionMatrix);
        float tickDelta = tickCounter.getGameTimeDeltaPartialTick(false);
        RenderEvent renderEvent = RenderEvent.INSTANCE.get(matrixStack, tickDelta);
        EventManager.Companion.getEventManager().call(renderEvent); // ← ここ
    }
}

