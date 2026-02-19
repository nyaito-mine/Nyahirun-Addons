package co.skyblock.mixin;

import co.skyblock.utils.render.mixinterface.IMinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Minimal mixin to expose BufferBuilderStorage
 * for rendering utilities (lines, ESP, tracers).
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMinecraftClient {

    /**
     * Minecraft's internal buffer builder storage.
     */
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    /**
     * Expose BufferBuilderStorage via IMinecraftClient.
     */
    @Override
    public RenderBuffers getBufferBuilderStorage() {
        return renderBuffers;
    }
}

