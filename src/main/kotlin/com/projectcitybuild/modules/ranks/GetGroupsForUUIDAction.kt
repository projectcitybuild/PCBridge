package com.projectcitybuild.modules.ranks

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.entities.models.ApiError
import com.projectcitybuild.core.entities.models.Group
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import java.util.*

class GetGroupsForUUIDAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class NetworkError: FailReason()
        class HTTPError(error: ApiError?): FailReason()
        class AccountNotLinked: FailReason()
        class Unhandled: FailReason()
    }

    suspend fun execute(playerId: UUID): Result<List<Group>, FailReason> {
        val authAPI = apiRequestFactory.pcb.authApi
        val response = apiClient.execute { authAPI.getUserGroups(uuid = playerId.toString()) }

        return when (response) {
            is APIResult.NetworkError -> Failure(FailReason.NetworkError())
            is APIResult.HTTPError -> {
                if (response.error?.id == "account_not_linked") {
                    Failure(FailReason.AccountNotLinked())
                }
                Failure(FailReason.HTTPError(response.error))
            }
            else -> Failure(FailReason.Unhandled())
        }
    }
}