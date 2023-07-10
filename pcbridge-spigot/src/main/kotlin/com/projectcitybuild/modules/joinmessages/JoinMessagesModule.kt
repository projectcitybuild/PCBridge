package com.projectcitybuild.modules.joinmessages

import com.projectcitybuild.modules.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.modules.joinmessages.listeners.AnnounceQuitListener
import com.projectcitybuild.modules.joinmessages.listeners.FirstTimeJoinListener
import com.projectcitybuild.modules.joinmessages.listeners.ServerOverviewJoinListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class JoinMessagesModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            listener(
                AnnounceJoinListener(container.server),
            )
            listener(
                AnnounceQuitListener(container.server),
            )
            listener(
                FirstTimeJoinListener(
                    container.server,
                    container.logger,
                ),
            )
            listener(
                ServerOverviewJoinListener(container.server),
            )
        }
    }
}