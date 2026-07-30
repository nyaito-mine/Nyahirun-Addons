package co.stellarskys.nyahirunaddons.api.render.world

import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.OutputTarget
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

object ApiRenderLayers {
    val LINES = RenderType.create(
        "lines",
        RenderSetup.builder(ApiRenderPipelines.LINES)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    )

    val LINES_DEPTH = RenderType.create(
        "lines_through_walls",
        RenderSetup.builder(ApiRenderPipelines.LINES_DEPTH)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    )

    val FILLED: RenderType = RenderType.create(
        "filled",
        RenderSetup.builder(ApiRenderPipelines.FILLED)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .sortOnUpload()
            .createRenderSetup()
    )

    val FILLED_DEPTH: RenderType = RenderType.create(
        "filled_through_walls",
        RenderSetup.builder(ApiRenderPipelines.FILLED_DEPTH)
            .sortOnUpload()
            .createRenderSetup()
    )
}