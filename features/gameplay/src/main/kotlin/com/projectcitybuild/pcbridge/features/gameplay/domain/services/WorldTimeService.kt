package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WorldSelector
import org.bukkit.Server
import org.bukkit.World

class WorldTimeService(
    private val server: Server,
) {
    private val timeInputParser = WorldTimeInputParser()

    data class WorldTimeChange(
        val world: World,
        val time: Long,
        val relativeTicks: Long?,
    )

    sealed interface SetResult {
        data class InvalidTime(
            val input: String,
        ) : SetResult

        data class WorldNotFound(
            val name: String,
        ) : SetResult

        data object WorldRequired : SetResult

        data class Updated(
            val changes: List<WorldTimeChange>,
        ) : SetResult
    }

    fun worldNames(): List<String> = server.worlds
        .map { it.name } + ALL_WORLDS_SELECTOR

    fun set(
        input: String,
        selector: WorldSelector,
        defaultWorld: World?,
    ): SetResult {
        val parsedTime = timeInputParser.parse(input)
            ?: return SetResult.InvalidTime(input)

        val worlds = when (selector) {
            is WorldSelector.Named -> {
                val world = server.getWorld(selector.name)
                    ?: return SetResult.WorldNotFound(selector.name)
                listOf(world)
            }
            is WorldSelector.Current -> {
                listOfNotNull(defaultWorld)
            }
            is WorldSelector.All -> {
                server.worlds
            }
        }
        if (worlds.isEmpty()) return SetResult.WorldRequired

        val updates = try {
            worlds.map { world ->
                val currentFullTime = world.fullTime
                val newFullTime = if (parsedTime.relative) {
                    Math.addExact(currentFullTime, parsedTime.ticks)
                } else {
                    val dayStart = currentFullTime - currentFullTime.mod(WorldTimeInputParser.TICKS_PER_DAY)
                    Math.addExact(
                        Math.addExact(dayStart, WorldTimeInputParser.TICKS_PER_DAY),
                        parsedTime.ticks.mod(WorldTimeInputParser.TICKS_PER_DAY),
                    )
                }
                world to newFullTime
            }
        } catch (_: ArithmeticException) {
            return SetResult.InvalidTime(input)
        }

        updates.forEach { (world, fullTime) ->
            world.fullTime = fullTime
        }

        return SetResult.Updated(
            updates.map { (world, fullTime) ->
                WorldTimeChange(
                    world = world,
                    time = fullTime.mod(WorldTimeInputParser.TICKS_PER_DAY),
                    relativeTicks = parsedTime.ticks.takeIf { parsedTime.relative },
                )
            },
        )
    }

    private companion object {
        const val ALL_WORLDS_SELECTOR = "@all"
    }
}
