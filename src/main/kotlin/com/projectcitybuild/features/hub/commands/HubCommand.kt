package com.projectcitybuild.features.hub.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.hub.repositories.HubRepository
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

class HubCommand @Inject constructor(
    private val plugin: Plugin,
    private val hubRepository: HubRepository,
    private val queuedWarpRepository: QueuedWarpRepository,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
    private val localEventBroadcaster: LocalEventBroadcaster,
): SpigotCommand {

    override val label: String = "hub"
    override val permission = "pcbridge.hub.use"
    override val usageHelp = "/hub"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val hub = hubRepository.get()
        if (hub == null) {
            input.sender.send().error("Hub has not been set")
            return
        }

        localEventBroadcaster.emit(
            PlayerPreWarpEvent(input.sender, input.sender.location)
        )

        val currentServerName = config.get(ConfigKey.SPIGOT_SERVER_NAME)
        val isHubOnSameServer = currentServerName == hub.serverName
        if (isHubOnSameServer) {
            val worldName = hub.worldName
            val world = plugin.server.getWorld(worldName)
            if (world == null) {
                logger.warning("Could not find world matching name [$worldName] for warp")
                input.sender.send().error("The target server is either offline or invalid")
                return
            }
            input.sender.teleport(
                Location(
                    world,
                    hub.x,
                    hub.y,
                    hub.z,
                    hub.yaw,
                    hub.pitch,
                )
            )
        } else {
            val warp = Warp(
                name = "/hub",
                location = hub,
                createdAt = LocalDateTime.now() // Not needed
            )
            queuedWarpRepository.queue(input.sender.uniqueId, warp)

            MessageToBungeecord(
                plugin,
                input.sender,
                SubChannel.SWITCH_PLAYER_SERVER,
                arrayOf(
                    input.sender.uniqueId.toString(),
                    hub.serverName,
                )
            ).send()
        }
    }
}
