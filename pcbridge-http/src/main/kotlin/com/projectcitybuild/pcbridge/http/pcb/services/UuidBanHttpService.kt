package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class UuidBanHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun create(
        bannedUUID: UUID,
        bannedAlias: String,
        bannerUUID: UUID?,
        bannerAlias: String?,
        reason: String,
        additionalInfo: String?,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().createUuidBan(
                bannedUUID = bannedUUID.toString(),
                bannedAlias = bannedAlias,
                bannerUUID = bannerUUID?.toString(),
                bannerAlias = bannerAlias,
                reason = reason,
                additionalInfo = additionalInfo,
            )
        }
    }
}
