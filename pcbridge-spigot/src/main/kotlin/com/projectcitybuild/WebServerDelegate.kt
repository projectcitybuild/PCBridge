package com.projectcitybuild

import com.projectcitybuild.modules.ranksync.actions.UpdatePlayerGroups
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.contracts.PlatformScheduler
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.webserver.HttpServerDelegate
import com.projectcitybuild.support.textcomponent.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Server

class WebServerDelegate(
    private val scheduler: PlatformScheduler,
    private val minecraftServer: Server,
    private val logger: PlatformLogger,
    private val updatePlayerGroups: UpdatePlayerGroups,
) : HttpServerDelegate {

    override fun syncPlayer(uuid: String) {
        val player = minecraftServer.onlinePlayers.firstOrNull {
            it.uniqueId.toString().unformatted() == uuid.unformatted()
        }
        if (player == null) {
            logger.info("No matching player found for sync request UUID: $uuid")
            return
        }

        logger.info("Syncing player: $uuid")

        scheduler.async<Result<Unit, UpdatePlayerGroups.FailureReason>> { resolver ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = updatePlayerGroups.execute(playerUUID = player.uniqueId)
                resolver(result)
            }
        }.startAndSubscribe { result ->
            when (result) {
                is Failure -> when (result.reason) {
                    UpdatePlayerGroups.FailureReason.ACCOUNT_NOT_LINKED
                    -> player.send().error("Your rank failed to be updated. Please contact a staff member")
                }

                is Success -> {
                    player.send().success("Your rank has been updated")
                }
            }
        }
    }
}

private fun String.unformatted(): String {
    return lowercase().replace(oldValue = "-", newValue = "")
}
