package com.projectcitybuild.pcbridge.paper.features.moderate

import com.projectcitybuild.pcbridge.paper.features.moderate.hooks.commands.KickCommand
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val moderateModule =
    module {
        factoryOf(::KickCommand)

        paperFeature("moderate") {
            commands(get<KickCommand>())
        }
    }
