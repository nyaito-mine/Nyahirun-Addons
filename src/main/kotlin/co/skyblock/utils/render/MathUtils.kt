package co.skyblock.utils.render

import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

object MathUtils {

    fun lerp(delta: Float, pos: Vec3, oldPos: Vec3): Vec3 {
        val x = Mth.lerp(delta.toDouble(), oldPos.x, pos.x)
        val y = Mth.lerp(delta.toDouble(), oldPos.y, pos.y)
        val z = Mth.lerp(delta.toDouble(), oldPos.z, pos.z)
        return Vec3(x, y, z)
    }
}
