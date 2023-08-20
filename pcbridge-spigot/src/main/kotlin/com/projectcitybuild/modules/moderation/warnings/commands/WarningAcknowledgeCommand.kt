package com.projectcitybuild.modules.moderation.warnings.commands

import com.projectcitybuild.modules.moderation.warnings.actions.AcknowledgeWarning
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class WarningAcknowledgeCommand(
    private val acknowledgeWarning: AcknowledgeWarning,
) {
    suspend fun execute(commandSender: Player, warningId: Int) {
        acknowledgeWarning.execute(warningId)
        commandSender.send().success("Warning acknowledged and hidden")
    }
}
