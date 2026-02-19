package co.skyblock.utils.render.listeners

import com.mojang.blaze3d.vertex.PoseStack

interface RenderListener : Listener {

    /**
     * Called when the world is rendered.
     *
     * @param matrixStack The matrix stack.
     * @param partialTicks The partial ticks.
     */
    fun onRender(matrixStack: PoseStack, partialTicks: Float)
}
