package com.projectcitybuild.pcbridge.paper.integrations.essentials

import com.projectcitybuild.pcbridge.paper.runtime.integrations.PaperIntegration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val essentialsModule =
    module {
        singleOf(::EssentialsIntegration) bind PaperIntegration::class
    }
