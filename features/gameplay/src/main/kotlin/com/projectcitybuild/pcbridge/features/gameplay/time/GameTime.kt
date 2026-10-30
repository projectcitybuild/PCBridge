package com.projectcitybuild.pcbridge.features.gameplay.time

import com.projectcitybuild.pcbridge.platform.paper.data.GameTimeAlias
import java.util.Locale

internal object GameTime {
    private const val TICKS_PER_DAY = 24_000L
    private const val TICKS_AT_MIDNIGHT = 18_000L
    private const val TICKS_PER_HOUR = 1_000L

    private val tickAmount = Regex("^(\\d+)(?:t|tick|ticks)?$")
    private val time24 = Regex("^(\\d{2}):?(\\d{2})$")
    private val time12 = Regex("^(\\d{1,2}):?(\\d{2})?(am|pm)$")

    fun parseTicks(input: String): Long? {
        val value = input.trim().lowercase()
        GameTimeAlias.from(value)?.let {
            return it.ticks
        }
        tickAmount.matchEntire(value)?.let {
            return it.groupValues[1].toLongOrNull()?.rem(TICKS_PER_DAY)
        }

        val match24 = time24.matchEntire(value)
        if (match24 != null) {
            val hours = match24.groupValues[1].toInt()
            val minutes = match24.groupValues[2].toInt()
            if (hours !in 0..23 || minutes !in 0..59) return null
            return hoursToTicks(
                hours,
                minutes,
            )
        }

        val match12 = time12.matchEntire(value) ?: return null
        var hours = match12.groupValues[1].toInt()
        val minutes = match12.groupValues[2].toIntOrNull() ?: 0
        if (hours !in 1..12 || minutes !in 0..59) return null
        if (match12.groupValues[3] == "pm" && hours != 12) {
            hours += 12
        } else if (match12.groupValues[3] == "am" && hours == 12) {
            hours = 0
        }
        return hoursToTicks(hours, minutes)
    }

    fun parseAddTicks(input: String): Long? {
        val value = input.trim().lowercase(Locale.ROOT)
        tickAmount.matchEntire(value)?.let { return it.groupValues[1].toLongOrNull() }
        return parseTicks(value)
    }

    fun parsePlayerTime(input: String): PlayerTimeInput? {
        val relative = !input.startsWith("@")
        val timeInput = if (relative) input else input.drop(1)
        return parseTicks(timeInput)?.let { PlayerTimeInput(it, relative) }
    }

    fun formatTicks(ticks: Long): String = "${ticks.mod(TICKS_PER_DAY)} ticks"

    fun formatTickAmount(ticks: Long): String = "$ticks ticks"

    private fun hoursToTicks(
        hours: Int,
        minutes: Int,
    ): Long = (TICKS_AT_MIDNIGHT + hours * TICKS_PER_HOUR + minutes * TICKS_PER_HOUR / 60) % TICKS_PER_DAY
}

internal data class PlayerTimeInput(
    val ticks: Long,
    val relative: Boolean,
)
