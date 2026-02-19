package co.skyblock.utils.render.mixinterface

import net.minecraft.client.renderer.RenderBuffers

interface IMinecraftClient {
    fun getBufferBuilderStorage(): RenderBuffers
}
