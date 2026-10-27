package com.projectcitybuild.pcbridge.runtime.teleportation

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block

class SafeYLocationFinder {
    fun findY(
        world: World,
        x: Int,
        z: Int,
    ): Int? = findY(
        world = world,
        x = x,
        z = z,
        isSafeFloor = { block ->
            block.type.let { it.isSolid && !it.isDangerous() }
        },
        isAir = { it.type.isAirBlock() },
    )

    internal fun findY(
        world: World,
        x: Int,
        z: Int,
        isSafeFloor: (Block) -> Boolean,
        isAir: (Block) -> Boolean,
    ): Int? = teleportationTracer.traceSync("SafeYLocationFinder.findY") { span ->
        val startedAtNanos = System.nanoTime()
        var verticalPositionsChecked = 0L
        var blockLookups = 0L
        var outcome = "error"

        try {
            if (!world.isChunkLoaded(x shr 4, z shr 4)) {
                outcome = "chunk_unloaded"
                return@traceSync null
            }

            val highestY = if (world.environment == World.Environment.NETHER) {
                world.maxHeight - 32
            } else {
                world.maxHeight - 3
            }.coerceAtLeast(world.minHeight)

            for (y in highestY downTo world.minHeight) {
                verticalPositionsChecked++
                blockLookups++
                val floor = world.getBlockAt(x, y, z)
                if (!isSafeFloor(floor)) continue

                blockLookups++
                val above = world.getBlockAt(x, y + 1, z)
                if (!isAir(above)) continue

                blockLookups++
                val headroom = world.getBlockAt(x, y + 2, z)
                if (isAir(headroom)) {
                    outcome = "found"
                    return@traceSync y + 1
                }
            }

            outcome = "not_found"
            null
        } finally {
            span.setAttribute("safe_y.search_duration_ns", System.nanoTime() - startedAtNanos)
            span.setAttribute("safe_y.vertical_positions_checked", verticalPositionsChecked)
            span.setAttribute("safe_y.block_lookups", blockLookups)
            span.setAttribute("safe_y.outcome", outcome)
            span.setAttribute("safe_y.failed", outcome != "found")
        }
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
