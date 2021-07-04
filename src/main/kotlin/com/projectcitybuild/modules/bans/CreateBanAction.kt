package com.projectcitybuild.modules.bans

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.core.entities.models.ApiError
import com.projectcitybuild.core.entities.models.ApiResponse
import com.projectcitybuild.core.entities.models.GameBan
import com.projectcitybuild.core.network.NetworkClients
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.util.*

class CreateBanAction(
        private val networkClients: NetworkClients
) {
    sealed class Result {
        class SUCCESS : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        PLAYER_ALREADY_BANNED,
        DESERIALIZE_FAILED,
        BAD_REQUEST,
        UNEXPECTED_EMPTY_BODY,
        UNHANDLED,
    }

    fun execute(playerId: UUID, playerName: String, staffId: UUID?, reason: String?) : Result {
        val banApi = networkClients.pcb.banApi

        val request = banApi.storeBan(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                playerAlias = playerName,
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid",
                reason = if (reason.isNullOrEmpty()) null else reason,
                expiresAt = null,
                isGlobalBan = 1
        )
        val response = request.execute()

        if (response.isSuccessful) {
            if (response.body()?.data == null) {
                return Result.FAILED(reason = Failure.UNEXPECTED_EMPTY_BODY)
            }
            return Result.SUCCESS()

        } else {
            // FIXME: this should be one layer higher
            val errorJson = response.errorBody()?.string()
            if (errorJson == null) {
                return Result.FAILED(Failure.UNEXPECTED_EMPTY_BODY)
            }

            try {
                val gson = Gson()
                val type = object : TypeToken<ApiResponse<GameBan>>() {}.type // Cannot do ApiResponse<GameBan>::class.java
                val errorModel: ApiResponse<GameBan> = gson.fromJson(errorJson, type)
                if (errorModel.error != null) {
                    return when (errorModel.error.id) {
                        "player_already_banned" -> Result.FAILED(reason = Failure.PLAYER_ALREADY_BANNED)
                        "bad_input" -> Result.FAILED(reason = Failure.BAD_REQUEST)
                        else -> Result.FAILED(reason = Failure.UNHANDLED)
                    }
                }
                return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)

            } catch (e: IOException) {
                e.printStackTrace()
                return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)
            }
        }
    }
}
