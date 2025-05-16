package com.projectcitybuild.pcbridge.paper.features.warnings

import com.projectcitybuild.pcbridge.paper.features.warnings.commands.WarnCommand
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val warningsModule = module {
    factory {
        WarnCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            manageUrlGenerator = get(),
        )
    }
}