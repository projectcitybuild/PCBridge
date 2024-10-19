package com.projectcitybuild.pcbridge

import com.projectcitybuild.pcbridge.webserver.HttpServerDelegate
import java.util.UUID

class WebServerDelegate() : HttpServerDelegate {

    override fun syncPlayer(uuid: UUID) {
        // val player = minecraftServer.onlinePlayers.firstOrNull {
        //     it.uniqueId.toString().unformatted() == uuid.unformatted()
        // }
        // if (player == null) {
        //     logger.info("No matching player found for sync request UUID: $uuid")
        //     return
        // }
        //
        // log.info { "Syncing player: $uuid" }
        //
        // scheduler.async<Result<Unit, UpdatePlayerGroups.FailureReason>> { resolver ->
        //     CoroutineScope(Dispatchers.IO).launch {
        //         val result = updatePlayerGroups.execute(playerUUID = player.uniqueId)
        //         resolver(result)
        //     }
        // }.startAndSubscribe { result ->
        //     when (result) {
        //         is Failure -> when (result.reason) {
        //             UpdatePlayerGroups.FailureReason.ACCOUNT_NOT_LINKED
        //             -> player.send().error("Your rank failed to be updated. Please contact a staff member")
        //         }
        //
        //         is Success -> {
        //             player.send().success("Your rank has been updated")
        //         }
        //     }
        // }
    }
}