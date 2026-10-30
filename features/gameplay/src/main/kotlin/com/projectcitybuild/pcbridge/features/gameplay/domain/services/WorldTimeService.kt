package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import org.bukkit.Server
import org.bukkit.World

class WorldTimeService(
    private val server: Server,
) {
    fun worldNames(): List<String> = server.worlds.map { it.name } + listOf("*", "all")

    fun resolveWorlds(
        selector: String?,
        defaultWorld: World?,
    ): WorldTimeResolution {
        if (selector == null) {
            return WorldTimeResolution.Found(defaultWorld?.let(::listOf) ?: server.worlds)
        }
        if (selector.equals("all", ignoreCase = true) || selector == "*") {
            return WorldTimeResolution.Found(server.worlds)
        }
        val world = server.getWorld(selector) ?: return WorldTimeResolution.Missing(selector)
        return WorldTimeResolution.Found(listOf(world))
    }

    fun current(world: World): Long = world.time

    fun set(
        world: World,
        ticks: Long,
    ) {
        val fullTime = world.fullTime
        world.fullTime = fullTime - fullTime % TICKS_PER_DAY + TICKS_PER_DAY + ticks
    }

    fun add(
        world: World,
        ticks: Long,
    ) {
        world.fullTime += ticks
    }

    private companion object {
        const val TICKS_PER_DAY = 24_000L
    }
}

sealed interface WorldTimeResolution {
    data class Found(
        val worlds: List<World>,
    ) : WorldTimeResolution

    data class Missing(
        val name: String,
    ) : WorldTimeResolution
}
