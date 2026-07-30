package co.stellarskys.nyahirunaddons.api.render.world

import co.stellarskys.nyahirunaddons.NyahirunAddons
import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import java.util.Optional

object ApiRenderPipelines {
    val LINES: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(id( "lines"))
            .withCull(false)
            .build()
    )

    val LINES_DEPTH: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(id( "lines_depth"))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .build()
    )

    val FILLED: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(id("filled"))
            .withCull(false)
            .build()
    )

    val FILLED_DEPTH: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(id("filled_depth"))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .build()
    )

    private fun id(path: String) = Identifier.fromNamespaceAndPath(NyahirunAddons.NAMESPACE, path)
}