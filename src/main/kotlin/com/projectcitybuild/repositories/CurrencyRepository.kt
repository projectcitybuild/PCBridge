package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIClient
import com.projectcitybuild.core.http.APIRequestFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class CurrencyRepository @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
) {
    private data class CachedBalance(
        val balance: Int,
        val fetchedAt: LocalDateTime,
    )

    private val balanceCache = ConcurrentHashMap<UUID, CachedBalance>()
    private val fetchQueue = ConcurrentHashMap<UUID, Boolean>()

    private fun fetchBalanceInBackground(playerUUID: UUID) {
        if (fetchQueue[playerUUID] != null) {
            return
        }
        fetchQueue[playerUUID] = true

        CoroutineScope(Dispatchers.IO).launch {
            val response = apiClient.execute {
                apiRequestFactory.pcb.balanceApi.get(
                    uuid = playerUUID.toString(),
                )
            }
            balanceCache.set(
                key = playerUUID,
                value = CachedBalance(
                    balance = response.data?.balance ?: 0,
                    fetchedAt = LocalDateTime.now(),
                )
            )
            fetchQueue.remove(playerUUID)
        }
    }

    fun getBalance(playerUUID: UUID): Int {
        // GadgetsMenu not only hits this method a LOT, but expects the
        // value to be returned synchronously. We need to queue balance
        // fetches in the background and sacrifice real-time balances
        val staleDate = LocalDateTime.now().minusSeconds(20)
        val cachedBalance = balanceCache[playerUUID]

        if (cachedBalance == null || cachedBalance.fetchedAt.isBefore(staleDate)) {
            fetchBalanceInBackground(playerUUID)
        }
        if (cachedBalance == null) {
            // No balance was ever fetched, but we can't do anything about that...
            return 0
        }
        return cachedBalance.balance
    }

    fun deduct(playerUUID: UUID, amount: Int, reason: String) : Boolean {
        val cachedBalance = balanceCache[playerUUID]
        if (cachedBalance == null || cachedBalance.balance - amount < 0) {
            return false
        }
        CoroutineScope(Dispatchers.IO).launch {
            apiClient.execute {
                apiRequestFactory.pcb.balanceApi.deduct(
                    uuid = playerUUID.toString(),
                    amount = amount,
                    reason = reason,
                )
                balanceCache.set(
                    key = playerUUID,
                    value = CachedBalance(
                        balance = max(0, (balanceCache[playerUUID]?.balance ?: 0) - amount),
                        fetchedAt = LocalDateTime.now(),
                    )
                )
            }
        }
        return true
    }
}
