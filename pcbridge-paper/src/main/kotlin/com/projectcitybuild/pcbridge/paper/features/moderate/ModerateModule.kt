package com.projectcitybuild.pcbridge.paper.features.moderate

import com.projectcitybuild.pcbridge.paper.features.moderate.hooks.commands.KickCommand
import org.koin.dsl.module

val moderateModule = module {
    factory {
        KickCommand(
            plugin = get(),
            server = get(),
        )
    }
}