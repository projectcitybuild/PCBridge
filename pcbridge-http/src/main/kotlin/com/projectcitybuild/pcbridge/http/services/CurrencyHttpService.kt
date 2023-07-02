package com.projectcitybuild.pcbridge.http.services

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import com.projectcitybuild.pcbridge.http.responses.AccountBalance
import retrofit2.Retrofit
import java.util.UUID

class CurrencyHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun get(playerUUID: UUID): AccountBalance? {
        val response = responseParser.parse {
            retrofit.pcb().getBalance(
                uuid = playerUUID.toString(),
            )
        }
        return response.data
    }

    suspend fun deduct(playerUUID: UUID, amount: Int, reason: String) {
        responseParser.parse {
            retrofit.pcb().deductFromBalance(
                uuid = playerUUID.toString(),
                amount = amount,
                reason = reason,
            )
        }
    }
}