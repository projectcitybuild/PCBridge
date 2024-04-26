package com.projectcitybuild.modules.pluginutils.commands

import com.projectcitybuild.modules.pluginutils.actions.GetVersion
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class VersionCommand(
    private val getVersion: GetVersion,
) {
    fun execute(commandSender: Player) {
        val version = getVersion.execute()

        commandSender.send().info(
            "Running PCBridge v${version.version} (${version.commitHash})"
        )
    }
}