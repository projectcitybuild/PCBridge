package com.projectcitybuild.modules.pluginutils.commands

import com.projectcitybuild.modules.pluginutils.actions.ReloadPlugin
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class ReloadCommand(
    private val reloadPlugin: ReloadPlugin,
) {
    fun execute(commandSender: Player) {
        reloadPlugin.execute()
        commandSender.send().success("Caches flushed")
    }
}