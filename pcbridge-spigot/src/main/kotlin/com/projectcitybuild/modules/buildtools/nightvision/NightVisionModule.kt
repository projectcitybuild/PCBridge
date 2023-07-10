package com.projectcitybuild.modules.buildtools.nightvision

import com.projectcitybuild.modules.buildtools.nightvision.commands.NightVisionCommand
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class NightVisionModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(NightVisionCommand())
        }
    }
}