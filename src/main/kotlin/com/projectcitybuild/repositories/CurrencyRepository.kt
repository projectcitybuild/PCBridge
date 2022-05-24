package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIClient
import com.projectcitybuild.core.http.APIRequestFactory
import dagger.Reusable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.math.max

@Reusable
class CurrencyRepository @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
) {
    private data class CachedBalance(
        val balance: Int,
        val fetchedAt: LocalDateTime,
    )

    private val balanceCache: MutableMap<UUID, CachedBalance> = mutableMapOf()
    private val fetchQueue = ConcurrentHashMap<UUID, Unit>()

    fun getBalance(playerUUID: UUID): Int {
        val thresholdDate = LocalDateTime.now().minusSeconds(15)

        // GadgetsMenu hits this method a LOT
        val cached = balanceCache[playerUUID]
        if (cached != null && cached.fetchedAt.isAfter(thresholdDate)) {
            return cached.balance
        }

        // Causes thread death due if we don't queue the requests
        if (!fetchQueue.contains(playerUUID)) {
            fetchQueue[playerUUID] = Unit

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

        if (cached != null) {
            // Return last known balance
            return cached.balance
        }

        // Return 0 if no balance was ever fetched
        return 0
    }

    fun deduct(playerUUID: UUID, amount: Int, reason: String) {
        // GadgetMenu calls this function from a background thread
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
    }
}
