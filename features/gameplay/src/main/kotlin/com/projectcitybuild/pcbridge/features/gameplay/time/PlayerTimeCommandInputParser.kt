package com.projectcitybuild.pcbridge.features.gameplay.time

internal data class PlayerTimeCommandInput(
    val time: String,
    val playerName: String?,
)

internal object PlayerTimeCommandInputParser {
    fun parse(
        input: String,
        onlinePlayerNames: Set<String>,
    ): PlayerTimeCommandInput {
        val normalized = input.trimEnd()
        val separator = normalized.lastIndexOf(' ')
        if (separator <= 0) return PlayerTimeCommandInput(normalized, null)

        val time = normalized.substring(0, separator).trimEnd()
        val playerName = normalized.substring(separator + 1)
        val isTarget = playerName in onlinePlayerNames || GameTime.parsePlayerTime(time) != null
        return if (isTarget) {
            PlayerTimeCommandInput(time, playerName)
        } else {
            PlayerTimeCommandInput(normalized, null)
        }
    }
}
