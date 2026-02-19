package co.skyblock.utils.render

import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

class Rotation(
    val pitch: Float,
    val yaw: Float
) {

    fun asLookVec(): Vec3 {
        val radiansPerDegree = Mth.DEG_TO_RAD
        val pi = Mth.PI

        val newPitch = -Mth.wrapDegrees(pitch) * radiansPerDegree
        val cosPitch = -Mth.cos(newPitch)
        val sinPitch = Mth.sin(newPitch)

        val newYaw = -Mth.wrapDegrees(yaw) * radiansPerDegree - pi
        val cosYaw = Mth.cos(newYaw)
        val sinYaw = Mth.sin(newYaw)

        return Vec3(
            sinYaw * cosPitch.toDouble(),
            sinPitch.toDouble(),
            cosYaw * cosPitch.toDouble()
        )
    }
}