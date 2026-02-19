package co.skyblock.features.nyahirunaddons.na1.general

import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object efficientDB : Feature("efficientDB", island = SkyBlockIsland.THE_CATACOMBS) {

    val efficientDBConfig by config.property<Boolean>("efficientDB")

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
