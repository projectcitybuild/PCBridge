package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.platforms.spigot.environment.send

class MaintenanceCommand(): Commandable {

    override val label: String = "maintenance"
    override val permission: String = "pcbridge.maintenance"

    override suspend fun execute(input: CommandInput): CommandResult {
        input.sender.send().error("This command is temporarily disabled, sorry")
        return CommandResult.EXECUTED
    }
}