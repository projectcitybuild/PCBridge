package com.projectcitybuild.pcbridge.features.warnings

import com.projectcitybuild.pcbridge.features.warnings.commands.WarnCommand
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val warningsModule =
    module {
        paperFeature("warnings") {
            commands(get<WarnCommand>())
        }

        factoryOf(::WarnCommand)
    }
