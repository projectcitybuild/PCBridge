package com.projectcitybuild.modules.players

import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.entities.Failure
import com.projectcitybuild.entities.models.ApiError
import com.projectcitybuild.entities.Result
import com.projectcitybuild.entities.Success
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.entities.models.MojangPlayer
import com.projectcitybuild.core.network.APIClient
import java.util.*

class GetMojangPlayerAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    class PlayerNotFoundException: Exception()

    suspend fun execute(playerName: String, at: Long? = null): MojangPlayer {
        val mojangApi = apiRequestFactory.mojang.mojangApi
        return apiClient.execute { mojangApi.getMojangPlayer(playerName, timestamp = at) }
            ?: throw PlayerNotFoundException()
    }
}
