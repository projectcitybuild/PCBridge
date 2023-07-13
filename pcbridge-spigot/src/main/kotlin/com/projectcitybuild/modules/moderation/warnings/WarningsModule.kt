package com.projectcitybuild.modules.moderation.warnings

import com.projectcitybuild.features.warnings.usecases.AcknowledgeWarning
import com.projectcitybuild.features.warnings.usecases.GetUnacknowledgedWarnings
import com.projectcitybuild.modules.moderation.warnings.commands.WarningAcknowledgeCommand
import com.projectcitybuild.modules.moderation.warnings.listeners.NotifyWarningsOnJoinListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class WarningsModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(
                WarningAcknowledgeCommand(
                    AcknowledgeWarning(container.playerWarningRepository)
                ),
            )
            listener(
                NotifyWarningsOnJoinListener(
                    GetUnacknowledgedWarnings(
                        container.playerWarningRepository,
                        container.dateTimeFormatter,
                    ),
                ),
            )
        }
    }
}