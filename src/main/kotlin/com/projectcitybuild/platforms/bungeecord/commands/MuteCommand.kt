package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.modules.playercache.PlayerCache
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer

class MuteCommand(
    private val proxyServer: ProxyServer,
    private val playerCache: PlayerCache
): BungeecordCommand {

    override val label = "mute"
    override val permission = "pcbridge.chat.mute"

    override fun validate(input: BungeecordCommandInput): CommandResult {
        if (!input.hasArguments)
            return CommandResult.INVALID_INPUT

        return CommandResult.EXECUTED
    }

    override suspend fun execute(input: BungeecordCommandInput) {
//        val targetPlayerName = input.args.first()
//        val targetPlayer = proxyServer.players
//            .first { it.name.lowercase() == targetPlayerName.lowercase() }
//
//        if (targetPlayer == null) {
//            input.sender.send().error("Player $targetPlayerName not found")
//            return
//        }

//        playerCache.get(targetPlayer.uniqueId)

//        val player = playerStore.get(targetPlayer.uniqueId)
//            ?: throw Exception("Player $targetPlayerName missing from cache")
//
//        if (player.isMuted) {
//            input.sender.send().error("$targetPlayerName is already muted")
//            return CommandResult.INVALID_INPUT
//        }
//
//        player.isMuted = true
//        playerStore.put(player.uuid, player)
//
//        input.sender.send().success("${targetPlayer.name} has been muted")
//        targetPlayer.sendMessage("You have been muted by ${input.sender.name}")
//
    }
}