package com.projectcitybuild.pcbridge.features.gameplay.time

internal data class WorldTimeCommandInput(
    val time: String,
    val worldSelector: String?,
)

internal object WorldTimeCommandInputParser {
    fun parse(
        input: String,
        isValidTime: (String) -> Boolean,
    ): WorldTimeCommandInput {
        if (isValidTime(input)) return WorldTimeCommandInput(input, null)

        val separator = input.lastIndexOf(' ')
        if (separator <= 0) return WorldTimeCommandInput(input, null)

        val time = input.substring(0, separator).trimEnd()
        val worldSelector = input.substring(separator + 1).trim()
        return if (worldSelector.isNotEmpty() && isValidTime(time)) {
            WorldTimeCommandInput(time, worldSelector)
        } else {
            WorldTimeCommandInput(input, null)
        }
    }
}
