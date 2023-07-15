package com.projectcitybuild.modules.buildtools.nightvision

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.buildtools.nightvision.commands.NightVisionCommand
import com.projectcitybuild.support.commandapi.ToggleOption
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor

class NightVisionModule: PluginModule {
    override fun register(module: ModuleDeclaration) = module {
        command("nv") {
            withPermission(Permissions.COMMAND_BUILD_NIGHTVISION)
            withAliases("nightvision")
            withShortDescription("Toggles nightvision mode on or off")
            withOptionalArguments(
                MultiLiteralArgument("toggle", listOf("on", "off"))
            )
            executesPlayer(PlayerCommandExecutor { player, args ->
                val desiredState = when(args.get("toggle")) {
                    "on" -> ToggleOption.ON
                    "off" -> ToggleOption.OFF
                    else -> ToggleOption.UNSPECIFIED
                }
                NightVisionCommand().execute(player, desiredState)
            })
        }
    }
}