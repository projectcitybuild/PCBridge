package com.projectcitybuild.pcbridge.paper.features.warnings

import com.projectcitybuild.pcbridge.paper.features.warnings.commands.WarnCommand
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val warningsModule =
    module {
        factoryOf(::WarnCommand)

        paperFeature("warnings") {
            commands(get<WarnCommand>())
        }
    }
