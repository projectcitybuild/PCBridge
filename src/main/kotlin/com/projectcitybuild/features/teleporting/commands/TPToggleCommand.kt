package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender

class TPToggleCommand(
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "tptoggle"
    override val permission = "pcbridge.tp.toggle"
    override val usageHelp = "/tptoggle [on|off]"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val desiredState = input.args.firstOrNull()?.lowercase()
        if (input.args.size == 1 && (desiredState != "off" && desiredState != "on")) {
            throw InvalidCommandArgumentsException()
        }

        val playerConfig = playerConfigRepository.get(input.player.uniqueId)

        // Either use the given toggle value, or reverse the current saved value
        val willToggleOn = if (input.args.size == 1) desiredState == "on" else !playerConfig.isAllowingTPs

        when {
            willToggleOn && playerConfig.isAllowingTPs
                -> input.sender.send().error("Already allowing teleports")

            !willToggleOn && !playerConfig.isAllowingTPs
                -> input.sender.send().error("Already disallowing teleports")

            else -> {
                playerConfig.isAllowingTPs = willToggleOn
                playerConfigRepository.save(playerConfig)

                input.sender.send().success(
                    if (willToggleOn) "Players can now teleport to or summon you"
                    else "Players can no longer teleport to or summon you"
                )
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("on", "off")
            else -> null
        }
    }
}