package com.projectcitybuild.pcbridge.integrations.essentials

import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val essentialsModule =
    module {
        singleOf(::EssentialsIntegration) bind PaperIntegration::class
    }
