package co.stellarskys.nyahirunaddons.features.dungeon.renderHighlight

import co.stellarskys.nyahirunaddons.api.render.world.Render3D
import co.stellarskys.nyahirunaddons.events.core.RenderEvent
import co.stellarskys.nyahirunaddons.features.RenderHighlight
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.api.dungeons.Dungeon
import co.stellarskys.stella.events.core.LocationEvent
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.api.zenith.player
import co.stellarskys.stella.api.zenith.world
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.entity.player.Player
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object StarredMob : Feature("RenderHighlight", island = SkyBlockIsland.THE_CATACOMBS) {
    private val DungeonMobSpawns = hashSetOf("Lurker", "Dreadlord", "Souleater", "Zombie", "Skeleton", "Skeletor", "Sniper", "Super Archer", "Spider", "Fels", "Withermancer", "Lost Adventurer", "Angry Archaeologist", "Frozen Adventurer")
    private val PlayerDungeonMobSpawns = hashSetOf("Shadow Assassin", "King Midas")
    private val StarredRegex = Regex("^.*✯ .*\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?(?:[kM])?❤$")
    private val TargetList = mutableSetOf<Entity>()


    override fun initialize() {
        on<TickEvent.Client> {
            if (!RenderHighlight.StarredMob || Dungeon.inBoss) {
                TargetList.clear()
                return@on
            }

            val entities = world?.entitiesForRendering() ?: return@on
            for (entity in entities) {
                if (entity is ArmorStand && entity.isAlive) {
                    val entityName = entity.name.string
                    if (
                        DungeonMobSpawns.any { it in entityName } &&
                        StarredRegex.matches(entityName)
                    ) {
                        world?.getEntities(entity, entity.boundingBox.move(0.0, -1.0, 0.0)) { isValidEntity(it) }
                            ?.firstOrNull()?.let { entity -> TargetList.add(entity) }
                    }
                }

                if (entity is Player && entity.isAlive && entity.uuid.version() == 2 && entity != player) {
                    val entityName = entity.name.string
                    if (
                        PlayerDungeonMobSpawns.any { it in entityName }
                    ) {
                        TargetList.add(entity)
                    }
                }
            }
            TargetList.removeIf { entity -> !entity.isAlive }
        }

        on<LocationEvent.ServerChange> {
            TargetList.clear()
        }

        on<RenderEvent.Draw> { event ->
            if (!RenderHighlight.StarredMob || Dungeon.inBoss) return@on
            for (entity in TargetList) {
                var box = Render3D.getLerpedBoxForBox(entity)
                if (RenderHighlight.StarredMobScale >= 1) box = box.inflate(RenderHighlight.StarredMobScale / 20.0)

                Render3D.drawBox(
                    event.context,
                    box,
                    RenderHighlight.StarredMobLineColor,
                    RenderHighlight.StarredMobFillColor.takeIf { RenderHighlight.StarredMobFill },
                    false
                )
            }
        }
    }

    private fun isValidEntity(entity: Entity): Boolean =
        when (entity) {
            is ArmorStand -> false
            is WitherBoss -> false
            is EnderMan -> true
            is Player -> entity.uuid.version() == 2 && entity != player
            else -> !entity.isInvisible
        }
}