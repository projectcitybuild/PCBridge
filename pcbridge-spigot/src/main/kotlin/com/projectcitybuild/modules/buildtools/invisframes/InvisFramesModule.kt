package com.projectcitybuild.modules.buildtools.invisframes

import com.projectcitybuild.modules.buildtools.invisframes.commands.InvisFrameCommand
import com.projectcitybuild.modules.buildtools.invisframes.listeners.FramePlaceListener
import com.projectcitybuild.modules.buildtools.invisframes.listeners.ItemInsertListener
import com.projectcitybuild.modules.buildtools.invisframes.listeners.ItemRemoveListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class InvisFramesModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(
                InvisFrameCommand(container.spigotNamespace),
            )
            listener(
                FramePlaceListener(container.spigotNamespace),
            )
            listener(
                ItemInsertListener(container.spigotNamespace),
            )
            listener(
                ItemRemoveListener(container.spigotNamespace),
            )
        }
    }
}