package com.projectcitybuild.modules.mutes

import com.projectcitybuild.entities.Permissions
import com.projectcitybuild.modules.mutes.commands.MuteCommand
import com.projectcitybuild.modules.mutes.commands.UnmuteCommand
import com.projectcitybuild.modules.mutes.middleware.MuteChatMiddleware
import com.projectcitybuild.pcbridge.core.architecture.events.EventPipeline
import com.projectcitybuild.pcbridge.core.architecture.features.PluginFeature
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.EntitySelectorArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import kotlin.coroutines.CoroutineContext

class MutesFeature(
    eventPipeline: EventPipeline,
    contextBuilder: () -> CoroutineContext,
    private val muteCommand: MuteCommand,
    private val unmuteCommand: UnmuteCommand,
    private val muteChatMiddleware: MuteChatMiddleware,
): PluginFeature(eventPipeline, contextBuilder) {

    override fun onLoad() {
        middleware.guardEvent(
            AsyncPlayerChatEvent::class.java,
            muteChatMiddleware,
        )

        CommandAPICommand("mute").apply {
            withPermission(Permissions.COMMAND_MUTES_MUTE)
            withShortDescription("Prevents a player from talking in chat")
            withArguments(
                EntitySelectorArgument.OnePlayer("player"),
            )
            executesPlayer(PlayerCommandExecutor { player, args ->
                muteCommand.execute(
                    commandSender = player,
                    targetPlayer = args.get("player") as Player,
                )
            })
            register()
        }

        CommandAPICommand("unmute").apply {
            withPermission(Permissions.COMMAND_MUTES_UNMUTE)
            withShortDescription("Allows a muted player to talk in chat again")
            withArguments(
                EntitySelectorArgument.OnePlayer("player"),
            )
            executesPlayer(PlayerCommandExecutor { player, args ->
                unmuteCommand.execute(
                    commandSender = player,
                    targetPlayer = args.get("player") as Player,
                )
            })
            register()
        }
    }
}