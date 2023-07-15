package com.projectcitybuild.modules.moderation.mutes

import com.projectcitybuild.modules.moderation.mutes.commands.MuteCommand
import com.projectcitybuild.modules.moderation.mutes.commands.UnmuteCommand
import com.projectcitybuild.modules.moderation.mutes.actions.MutePlayer
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class MutesModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            legacyCommand(
                MuteCommand(
                    container.server,
                    MutePlayer(
                        container.playerConfigRepository,
                        container.nameGuesser,
                    )
                ),
            )
            legacyCommand(
                UnmuteCommand(
                    container.server,
                    MutePlayer(
                        container.playerConfigRepository,
                        container.nameGuesser,
                    ),
                ),
            )
        }
    }
}