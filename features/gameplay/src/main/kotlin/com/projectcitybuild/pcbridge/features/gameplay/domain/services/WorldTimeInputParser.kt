package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import com.projectcitybuild.pcbridge.platform.paper.data.GameTimeAlias
import java.util.Locale

class WorldTimeInputParser {
    data class ParsedTime(
        val ticks: Long,
        val relative: Boolean,
    )

    fun parse(input: String): ParsedTime? {
        val value = input.trim().lowercase(Locale.ROOT)
        val relative = value.startsWith('+') || value.startsWith('-')
        val timeInput = if (relative) value.drop(1) else value
        val ticks = parseAmount(timeInput) ?: return null

        return ParsedTime(
            ticks = if (value.startsWith('-')) -ticks else ticks,
            relative = relative,
        )
    }

    private fun parseAmount(input: String): Long? {
        GameTimeAlias.from(input)?.let {
            return it.ticks
        }
        val ticks = TICK_PATTERN.matchEntire(input)
        if (ticks != null) {
            return ticks.groupValues[1].toLongOrNull()
        }
        val duration = DURATION_PATTERN.matchEntire(input)
        if (duration != null) {
            val amount = duration.groupValues[1].toLongOrNull() ?: return null
            val multiplier = when (duration.groupValues[2]) {
                "d" -> TICKS_PER_DAY
                "h" -> TICKS_PER_HOUR
                "m" -> TICKS_PER_MINUTE
                "s" -> TICKS_PER_SECOND
                else -> return null
            }
            return amount
                .takeIf { it <= Long.MAX_VALUE / multiplier }
                ?.times(multiplier)
        }

        val clock24 = CLOCK_24_PATTERN.matchEntire(input)
        if (clock24 != null) {
            val hours = clock24.groupValues[1].toIntOrNull() ?: return null
            val minutes = clock24.groupValues[2].toIntOrNull() ?: return null
            if (hours !in 0..23 || minutes !in 0..59) return null
            return TICKS_AT_MIDNIGHT + hours * TICKS_PER_HOUR + minutes * TICKS_PER_HOUR / 60
        }

        val clock12 = CLOCK_12_PATTERN.matchEntire(input) ?: return null
        var hours = clock12.groupValues[1].toIntOrNull() ?: return null
        val minutes = clock12.groupValues[2].toIntOrNull() ?: 0
        if (hours !in 1..12 || minutes !in 0..59) return null
        val period = clock12.groupValues[3]
        if (period == "pm" && hours != 12) hours += 12
        if (period == "am" && hours == 12) hours = 0
        return TICKS_AT_MIDNIGHT + hours * TICKS_PER_HOUR + minutes * TICKS_PER_HOUR / 60
    }

    companion object {
        private const val TICKS_PER_SECOND = 20L
        private const val TICKS_PER_MINUTE = 1_200L
        private const val TICKS_PER_HOUR = 1_000L
        private const val TICKS_AT_MIDNIGHT = 18_000L

        private val TICK_PATTERN = Regex("^(\\d+)(?:t|tick|ticks)?$")
        private val DURATION_PATTERN = Regex("^(\\d+)([dshm])$")
        private val CLOCK_24_PATTERN = Regex("^(\\d{1,2}):?(\\d{2})$")
        private val CLOCK_12_PATTERN = Regex("^(\\d{1,2}):?(\\d{2})?(am|pm)$")

        const val TICKS_PER_DAY = 24_000L
    }
}
