package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import com.projectcitybuild.pcbridge.http.responses.DonationPerk
import com.projectcitybuild.pcbridge.http.responses.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class PlayerGroupHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    class NoLinkedAccountException : Exception()

    @Throws(NoLinkedAccountException::class)
    suspend fun getGroups(playerUUID: UUID): List<Group> =
        withContext(Dispatchers.IO) {
            val response =
                try {
                    responseParser.parse {
                        retrofit.pcb().getUserGroups(
                            uuid = playerUUID.toString(),
                        )
                    }
                } catch (e: ResponseParser.HTTPError) {
                    if (e.errorBody?.id == "account_not_linked") {
                        throw NoLinkedAccountException()
                    }
                    throw e
                }
            response.data?.groups ?: listOf()
        }

    @Throws(NoLinkedAccountException::class)
    suspend fun getDonorPerks(playerUUID: UUID): List<DonationPerk> =
        withContext(Dispatchers.IO) {
            val response =
                try {
                    responseParser.parse {
                        retrofit.pcb().getDonationTier(
                            uuid = playerUUID.toString(),
                        )
                    }
                } catch (e: ResponseParser.HTTPError) {
                    if (e.errorBody?.id == "account_not_linked") {
                        throw NoLinkedAccountException()
                    }
                    throw e
                }
            response.data?.toList() ?: listOf()
        }
}
