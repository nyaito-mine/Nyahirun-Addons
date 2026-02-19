package co.skyblock.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import java.util.OptionalDouble

object GemRenderLayers {

    /* =========================
       Render Pipelines
       ========================= */

    val LINE_STRIP: RenderPipeline =
        RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation("gem/lines")
                .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                .build()
        )

    val ESP_LINE_STRIP: RenderPipeline =
        RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation("gem/lines")
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build()
        )

    val FILL_PIPELINE: RenderPipeline =
        RenderPipelines.register(
            RenderPipeline.builder(
                RenderPipelines.MATRICES_FOG_SNIPPET,
                RenderPipelines.DEBUG_FILLED_SNIPPET
            )
                .withLocation("gem/esp_fill")
                .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                .withCull(false)
                .build()
        )

    val ESP_FILL_PIPELINE: RenderPipeline =
        RenderPipelines.register(
            RenderPipeline.builder(
                RenderPipelines.MATRICES_FOG_SNIPPET,
                RenderPipelines.DEBUG_FILLED_SNIPPET
            )
                .withLocation("gem/esp_fill")
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withCull(false)
                .build()
        )

    /* =========================
       Render Layers
       ========================= */

    val LINES: RenderType.CompositeRenderType =
        RenderType.create(
            "gem:lines",
            1536,
            LINE_STRIP,
            RenderType.CompositeState.builder()
                .setLineState(RenderStateShard.LineStateShard(OptionalDouble.of(3.0)))
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .createCompositeState(false)
        )

    val ESP_LINES: RenderType.CompositeRenderType =
        RenderType.create(
            "gem:esp_lines",
            1536,
            ESP_LINE_STRIP,
            RenderType.CompositeState.builder()
                .setLineState(RenderStateShard.LineStateShard(OptionalDouble.of(3.0)))
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .createCompositeState(false)
        )

    val FILLS: RenderType.CompositeRenderType =
        RenderType.create(
            "gem:esp_fill",
            1536,
            FILL_PIPELINE,
            RenderType.CompositeState.builder()
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .createCompositeState(false)
        )

    val ESP_FILLS: RenderType.CompositeRenderType =
        RenderType.create(
            "gem:esp_fill",
            1536,
            ESP_FILL_PIPELINE,
            RenderType.CompositeState.builder()
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .createCompositeState(false)
        )
}
