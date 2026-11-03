package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import org.bukkit.entity.Player

class PlayerTimeService {
    private val timeInputParser = WorldTimeInputParser()

    sealed interface SetResult {
        data class InvalidTime(
            val input: String,
        ) : SetResult

        data class Updated(
            val ticks: Long,
            val relative: Boolean,
        ) : SetResult
    }

    fun isValid(input: String): Boolean = parse(input) != null

    fun set(
        player: Player,
        input: String,
    ): SetResult {
        val parsedTime = parse(input) ?: return SetResult.InvalidTime(input)

        val playerTime = try {
            val dayStart = player.playerTime - player.playerTime.mod(WorldTimeInputParser.TICKS_PER_DAY)
            val time = Math.addExact(
                Math.addExact(dayStart, WorldTimeInputParser.TICKS_PER_DAY),
                parsedTime.ticks,
            )
            if (parsedTime.relative) Math.subtractExact(time, player.world.time) else time
        } catch (_: ArithmeticException) {
            return SetResult.InvalidTime(input)
        }

        player.setPlayerTime(playerTime, parsedTime.relative)
        return SetResult.Updated(parsedTime.ticks, parsedTime.relative)
    }

    private fun parse(input: String): ParsedPlayerTime? {
        val fixed = input.startsWith('@')
        val timeInput = if (fixed) input.drop(1) else input
        val parsedTime = timeInputParser.parse(timeInput) ?: return null
        val ticks = if (parsedTime.relative) {
            parsedTime.ticks
        } else {
            parsedTime.ticks.mod(WorldTimeInputParser.TICKS_PER_DAY)
        }
        return ParsedPlayerTime(ticks, relative = !fixed)
    }

    private data class ParsedPlayerTime(
        val ticks: Long,
        val relative: Boolean,
    )
}
