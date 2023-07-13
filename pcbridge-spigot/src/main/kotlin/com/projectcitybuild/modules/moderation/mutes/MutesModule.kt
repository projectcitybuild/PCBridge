package com.projectcitybuild.modules.moderation.mutes

import com.projectcitybuild.modules.moderation.mutes.commands.MuteCommand
import com.projectcitybuild.modules.moderation.mutes.commands.UnmuteCommand
import com.projectcitybuild.features.chat.usecases.MutePlayer
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class MutesModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(
                MuteCommand(
                    container.server,
                    MutePlayer(
                        container.playerConfigRepository,
                        container.nameGuesser,
                    )
                ),
            )
            command(
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