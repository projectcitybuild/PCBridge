package com.projectcitybuild.spigot.modules.maintenance

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.modules.maintenance.commands.MaintenanceCommand
import com.projectcitybuild.spigot.modules.maintenance.listeners.MaintenanceConnectListener

class MaintenanceController : Controller {
    override val commands: Array<Commandable> = arrayOf(
            MaintenanceCommand()
    )

    override val listeners: Array<Listenable<*>> = arrayOf(
            MaintenanceConnectListener()
    )
}
