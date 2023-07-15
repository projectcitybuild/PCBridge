package com.projectcitybuild.modules.moderation.staffchat

import com.projectcitybuild.modules.moderation.staffchat.commands.ACommand
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class StaffChatModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            legacyCommand(
                ACommand(container.server),
            )
        }
    }
}