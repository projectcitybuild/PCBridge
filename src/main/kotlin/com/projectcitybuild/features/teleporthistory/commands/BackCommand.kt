package com.projectcitybuild.features.teleporthistory.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.teleporthistory.repositories.LastKnownLocationRepositoy
import com.projectcitybuild.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.time.LocalDateTime
import javax.inject.Inject

class BackCommand @Inject constructor(
    private val plugin: Plugin,
    private val lastKnownLocationRepositoy: LastKnownLocationRepositoy,
    private val queuedWarpRepository: QueuedWarpRepository,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
    private val localEventBroadcaster: LocalEventBroadcaster,
): SpigotCommand {

    override val label: String = "back"
    override val permission = "pcbridge.tp.back"
    override val usageHelp = "/back"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val lastKnownLocation = lastKnownLocationRepositoy.get(input.sender.uniqueId)
        if (lastKnownLocation == null) {
            input.sender.send().error("No last known location")
            return
        }

        localEventBroadcaster.emit(
            PlayerPreWarpEvent(input.sender, input.sender.location)
        )

        val currentServerName = config.get(ConfigKey.SPIGOT_SERVER_NAME)
        val isDestinationOnSameServer = currentServerName == lastKnownLocation.location.serverName
        if (isDestinationOnSameServer) {
            val worldName = lastKnownLocation.location.worldName
            val world = plugin.server.getWorld(worldName)
            if (world == null) {
                logger.warning("Could not find world matching name [$worldName] for warp")
                input.sender.send().error("The target server is either offline or invalid")
                return
            }
            input.sender.teleport(
                Location(
                    world,
                    lastKnownLocation.location.x,
                    lastKnownLocation.location.y,
                    lastKnownLocation.location.z,
                    lastKnownLocation.location.yaw,
                    lastKnownLocation.location.pitch,
                )
            )
        } else {
            val warp = Warp(
                name = "/back",
                location = lastKnownLocation.location,
                createdAt = LocalDateTime.now() // Not needed
            )
            queuedWarpRepository.queue(input.sender.uniqueId, warp)

            MessageToBungeecord(
                plugin,
                input.sender,
                SubChannel.SWITCH_PLAYER_SERVER,
                arrayOf(
                    input.sender.uniqueId.toString(),
                    warp.location.serverName,
                )
            ).send()
        }
    }
}
