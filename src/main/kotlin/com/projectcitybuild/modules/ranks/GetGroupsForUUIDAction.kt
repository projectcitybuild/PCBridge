package com.projectcitybuild.modules.ranks

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.entities.models.Group
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.modules.bans.CreateBanAction
import java.util.*

class GetGroupsForUUIDAction(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient
) {
    sealed class FailReason {
        class API_ERROR(val message: String): FailReason()
        class ACCOUNT_NOT_LINKED: FailReason()
        class UNHANDLED: FailReason()
    }

    fun execute(
        playerId: UUID,
        completion: (Result<List<Group>, FailReason>) -> Unit
    ) {
        val authAPI = apiRequestFactory.pcb.authApi
        val request = authAPI.getUserGroups(uuid = playerId.toString())

        apiClient.execute(request).startAndSubscribe { result ->
            when (result) {
                is Success -> completion(Success(result.value?.groups ?: listOf()))
                is Failure -> {
                    val responseBody = result.reason.responseBody
                    if (responseBody != null) {
                        val reason = when (responseBody.id) {
                            "account_not_linked" -> FailReason.ACCOUNT_NOT_LINKED()
                            else -> FailReason.UNHANDLED()
                        }
                        completion(Failure(reason))
                    } else {
                        completion(Failure(FailReason.API_ERROR(message = result.reason.message)))
                    }
                }
            }
        }
    }
}