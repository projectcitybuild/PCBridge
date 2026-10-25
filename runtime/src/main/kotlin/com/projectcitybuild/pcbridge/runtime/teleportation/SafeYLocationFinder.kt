package com.projectcitybuild.pcbridge.runtime.teleportation

import org.bukkit.Material
import org.bukkit.World

class SafeYLocationFinder {
    fun findY(
        world: World,
        x: Int,
        z: Int,
    ): Int? = teleportationTracer.traceSync("SafeYLocationFinder.findY") {
        if (!world.isChunkLoaded(x shr 4, z shr 4)) return@traceSync null

        val highestY = if (world.environment == World.Environment.NETHER) {
            world.maxHeight - 32 // Nether ceiling
        } else {
            world.maxHeight - 3 // Max build height minus player height
        }.coerceAtLeast(world.minHeight)

        for (y in highestY downTo world.minHeight) {
            val floor = world.getBlockAt(x, y, z)
            val above = world.getBlockAt(x, y + 1, z)
            val headroom = world.getBlockAt(x, y + 2, z)
            if (floor.type.isSolid && !floor.type.isDangerous() &&
                above.type.isAirBlock() && headroom.type.isAirBlock()
            ) {
                return@traceSync y + 1
            }
        }
        null
    }
}

private fun Material.isAirBlock() = this == Material.AIR ||
    this == Material.CAVE_AIR ||
    this == Material.VOID_AIR

private val dangerousMaterials = setOf(
    Material.LAVA,
    Material.WATER,
    Material.KELP,
    Material.KELP_PLANT,
    Material.SEAGRASS,
    Material.TALL_SEAGRASS,
    Material.CACTUS,
    Material.VOID_AIR,
)

private fun Material.isDangerous() = this in dangerousMaterials
