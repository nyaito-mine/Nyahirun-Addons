package co.stellarskys.nyahirunaddons.api.render.world

import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

class Rotation(
    val pitch: Float,
    val yaw: Float
) {

    fun asLookVec(): Vec3 {
        val radiansPerDegree = Mth.DEG_TO_RAD
        val pi = Mth.PI

        val newPitch: Float = -Mth.wrapDegrees(pitch) * radiansPerDegree
        val cosPitch = -Mth.cos(newPitch.toDouble())
        val sinPitch = Mth.sin(newPitch.toDouble())

        val newYaw = -Mth.wrapDegrees(yaw) * radiansPerDegree - pi
        val cosYaw = Mth.cos(newYaw.toDouble())
        val sinYaw = Mth.sin(newYaw.toDouble())

        return Vec3(
            sinYaw * cosPitch.toDouble(),
            sinPitch.toDouble(),
            cosYaw * cosPitch.toDouble()
        )
    }
}