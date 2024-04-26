package com.projectcitybuild.modules.moderation.warnings.actions

import com.projectcitybuild.pcbridge.core.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.repositories.PlayerWarningRepository
import java.util.UUID

class GetUnacknowledgedWarnings(
    private val playerWarningRepository: PlayerWarningRepository,
    private val dateTimeFormatter: DateTimeFormatter,
) {
    data class FormattedWarning(
        val id: Int,
        val reason: String,
        val isAcknowledged: Boolean,
        val createdAt: String,
    )

    suspend fun execute(
        playerUUID: UUID,
        playerName: String
    ): List<FormattedWarning> {
        return playerWarningRepository
            .get(playerUUID, playerName)
            .filter { !it.isAcknowledged }
            .map {
                FormattedWarning(
                    id = it.id,
                    reason = it.reason,
                    isAcknowledged = it.isAcknowledged,
                    createdAt = it.createdAt.let { createdAt ->
                        dateTimeFormatter.convert(createdAt)
                    },
                )
            }
    }
}
