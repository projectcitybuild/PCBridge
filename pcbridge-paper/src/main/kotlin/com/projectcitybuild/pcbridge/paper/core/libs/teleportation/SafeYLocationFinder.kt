package com.projectcitybuild.pcbridge.paper.core.libs.teleportation

import org.bukkit.Material
import org.bukkit.World

class SafeYLocationFinder {
    fun findY(world: World, x: Int, z: Int): Int? {
        val highestBlock = world.getHighestBlockAt(x, z)
        val aboveBlock = world.getBlockAt(x, highestBlock.y + 1, z)

        // Need headroom to teleport
        if (aboveBlock.type != Material.AIR) return null

        // Need a surface we can actually stand on
        if (!highestBlock.type.isSolid || highestBlock.type.isDangerous()) return null

        return highestBlock.y + 1
    }
}

private fun Material.isDangerous(): Boolean {
    return this in listOf(
        Material.LAVA,
        Material.WATER,
        Material.KELP, // Underwater vegetation
        Material.KELP_PLANT, // Underwater vegetation
        Material.SEAGRASS, // Underwater vegetation
        Material.TALL_SEAGRASS, // Underwater vegetation
        Material.CACTUS,
        Material.VOID_AIR,
    )
}