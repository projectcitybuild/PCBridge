package com.projectcitybuild.modules.teleport

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.teleport.commands.TeleportPositionCommand
import com.projectcitybuild.support.commandapi.suspendExecutesPlayer
import com.projectcitybuild.support.commandapi.worldArgument
import com.projectcitybuild.support.java.orNull
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.arguments.DoubleArgument
import dev.jorel.commandapi.arguments.FloatArgument
import org.bukkit.World

class TeleportModule: PluginModule {

    override fun register(module: ModuleDeclaration) = module {
        command("tppos") {
            withPermission(Permissions.COMMAND_TELEPORT_POSITION)
            withShortDescription("Teleports you to the specified coordinate at an optional yaw, pitch, and/or world")
            withArguments(
                DoubleArgument("x"),
                DoubleArgument("y"),
                DoubleArgument("z"),
            )
            withOptionalArguments(
                FloatArgument("yaw"),
                FloatArgument("pitch"),
                worldArgument("world"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                TeleportPositionCommand().execute(
                    commandSender = player,
                    x = args.get("x") as Double,
                    y = args.get("y") as Double,
                    z = args.get("z") as Double,
                    yaw = args.getOptional("yaw").map { it as Float }.orNull(),
                    pitch = args.getOptional("pitch").map { it as Float }.orNull(),
                    world = args.getOptional("world").map { it as World }.orNull(),
                )
            }
        }
    }
}
