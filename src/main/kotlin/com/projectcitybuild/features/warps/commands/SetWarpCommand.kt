package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import org.bukkit.entity.Player
import java.time.LocalDateTime
import javax.inject.Inject

class SetWarpCommand @Inject constructor(
    private val warpRepository: WarpRepository,
    private val config: PlatformConfig,
): SpigotCommand {

    override val label = "setwarp"
    override val permission = "pcbridge.warp.create"
    override val usageHelp = "/setwarp <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }
        val player = input.sender as? Player
        if (player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val warpName = input.args.first()

        if (warpRepository.exists(warpName)) {
            input.sender.send().error("A warp for $warpName already exists")
            return
        }

        val warp = Warp(
            warpName,
            CrossServerLocation.fromLocation(
                serverName = config.get(PluginConfig.SPIGOT_SERVER_NAME),
                location = player.location,
            ),
            LocalDateTime.now()
        )
        warpRepository.add(warp)

        input.sender.send().success("Created warp for $warpName")
    }
}
