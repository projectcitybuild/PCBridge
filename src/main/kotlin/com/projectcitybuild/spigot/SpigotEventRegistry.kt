package com.projectcitybuild.spigot

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.modules.bans.commands.BanCommand
import com.projectcitybuild.spigot.modules.bans.commands.CheckBanCommand
import com.projectcitybuild.spigot.modules.bans.commands.UnbanCommand
import com.projectcitybuild.spigot.modules.bans.listeners.BanConnectionListener
import com.projectcitybuild.spigot.modules.chat.commands.MuteCommand
import com.projectcitybuild.spigot.modules.chat.commands.UnmuteCommand
import com.projectcitybuild.spigot.modules.chat.listeners.ChatListener
import com.projectcitybuild.spigot.modules.maintenance.commands.MaintenanceCommand
import com.projectcitybuild.spigot.modules.maintenance.listeners.MaintenanceConnectListener
import com.projectcitybuild.spigot.modules.ranks.commands.SyncCommand
import com.projectcitybuild.spigot.modules.ranks.listeners.SyncRankLoginListener

class SpigotEventRegistry: Controller {
    override val commands: Array<Commandable> = arrayOf(
            BanCommand(),
            UnbanCommand(),
            CheckBanCommand(),
            MuteCommand(),
            UnmuteCommand(),
            MaintenanceCommand(),
            SyncCommand()
    )

    override val listeners: Array<Listenable<*>> = arrayOf(
            BanConnectionListener(),
            ChatListener(),
            MaintenanceConnectListener(),
            SyncRankLoginListener()
    )
}