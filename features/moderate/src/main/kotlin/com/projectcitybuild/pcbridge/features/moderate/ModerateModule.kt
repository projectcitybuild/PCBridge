package com.projectcitybuild.pcbridge.features.moderate

import com.projectcitybuild.pcbridge.features.moderate.hooks.commands.KickCommand
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val moderateModule =
    module {
        paperFeature("moderate") {
            commands(get<KickCommand>())
        }

        factoryOf(::KickCommand)
    }
