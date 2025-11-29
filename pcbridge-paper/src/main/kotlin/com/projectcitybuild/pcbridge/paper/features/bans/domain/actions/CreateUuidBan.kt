package com.projectcitybuild.pcbridge.paper.features.bans.domain.actions

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.core.libs.playerlookup.PlayerLookup
import com.projectcitybuild.pcbridge.paper.features.bans.bansTracer
import com.projectcitybuild.pcbridge.paper.features.bans.domain.repositories.UuidBanRepository
import java.util.UUID

class CreateUuidBan(
    private val playerLookup: PlayerLookup,
    private val uuidBanRepository: UuidBanRepository,
    private val manageUrlGenerator: ManageUrlGenerator,
) {
    data class Creation(
        val ban: PlayerBan,
        val bannedUuid: UUID,
        val editUrl: String,
    )

    class PlayerNotFound: Exception()
    class PlayerAlreadyBanned: Exception()
    class InvalidBanInput(message: String?): Exception(message)

    suspend fun create(
        bannedAlias: String,
        bannerUuid: UUID?,
        bannerAlias: String?,
        reason: String,
        additionalInfo: String?,
    ): Creation = bansTracer.trace("CreateUuidBan.create") {
        val uuid = playerLookup.findUuid(alias = bannedAlias)
            ?: throw PlayerNotFound()

        val ban = try {
            uuidBanRepository.create(
                bannedUUID = uuid,
                bannedAlias = bannedAlias,
                bannerUUID = bannerUuid,
                bannerAlias = bannerAlias,
                reason = reason,
                additionalInfo = additionalInfo,
            )
        } catch (_: ResponseParserError.Conflict) {
            throw PlayerAlreadyBanned()
        } catch (e: ResponseParserError.Validation) {
            throw InvalidBanInput(e.message)
        }

        val editUrl = manageUrlGenerator.path("manage/player-bans/${ban.id}/edit")

        return@trace Creation(
            ban = ban,
            bannedUuid = uuid,
            editUrl = editUrl,
        )
    }
}