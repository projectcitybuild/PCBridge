package com.projectcitybuild.features.warnings.actions

import com.projectcitybuild.core.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.features.warnings.repositories.PlayerWarningRepository
import java.util.UUID

class GetUnacknowledgedWarnings(
    private val warningRepository: PlayerWarningRepository,
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
        return warningRepository
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
