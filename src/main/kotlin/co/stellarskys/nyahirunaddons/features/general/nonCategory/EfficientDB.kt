package co.stellarskys.nyahirunaddons.features.general.nonCategory

import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object EfficientDB : Feature("EfficientDB", island = SkyBlockIsland.THE_CATACOMBS) {
    @JvmField
    val IGNORED_BLOCKS: Set<Block> = setOf(
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.LEVER,
        Blocks.SKELETON_SKULL,
        Blocks.REDSTONE_BLOCK
    )

    override fun initialize() {
    }
}