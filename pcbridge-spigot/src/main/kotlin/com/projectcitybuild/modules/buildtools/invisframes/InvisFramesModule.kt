package com.projectcitybuild.modules.buildtools.invisframes

import com.projectcitybuild.entities.Permissions
import com.projectcitybuild.modules.buildtools.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.modules.buildtools.invisframes.listeners.FramePlaceListener
import com.projectcitybuild.modules.buildtools.invisframes.listeners.ItemInsertListener
import com.projectcitybuild.modules.buildtools.invisframes.listeners.ItemRemoveListener
import com.projectcitybuild.pcbridge.core.architecture.events.EventPipeline
import com.projectcitybuild.pcbridge.core.architecture.features.PluginFeature
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import kotlin.coroutines.CoroutineContext

class InvisFramesModule(
    eventPipeline: EventPipeline,
    contextBuilder: () -> CoroutineContext,
    private val framePlaceListener: FramePlaceListener,
    private val itemInsertListener: ItemInsertListener,
    private val itemRemoveListener: ItemRemoveListener,
    private val invisFrameCommand: InvisFrameCommand,
): PluginModule, PluginFeature(eventPipeline, contextBuilder) {

    // TODO: remove
    override fun register(module: ModuleDeclaration) = module {}

    override fun onLoad() {
        events.subscribe(HangingPlaceEvent::class.java) { event ->
            framePlaceListener.handle(event)
        }
        events.subscribe(PlayerInteractEntityEvent::class.java) { event ->
            itemInsertListener.handle(event)
        }
        events.subscribe(EntityDamageByEntityEvent::class.java) { event ->
            itemRemoveListener.handle(event)
        }

        CommandAPICommand("invisframe").apply {
            withPermission(Permissions.COMMAND_BUILD_INVIS_FRAME)
            withShortDescription("Gives you an invisible item frame")
            withOptionalArguments(
                MultiLiteralArgument("type", listOf("glowing"))
            )
            executesPlayer(PlayerCommandExecutor { player, args ->
                val isGlowingFrame = args.get("type") == "glowing"

                invisFrameCommand.execute(player, isGlowingFrame)
            })
            register()
        }
    }
}