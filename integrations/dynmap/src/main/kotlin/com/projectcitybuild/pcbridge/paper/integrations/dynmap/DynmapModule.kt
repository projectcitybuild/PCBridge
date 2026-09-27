package com.projectcitybuild.pcbridge.paper.integrations.dynmap

import com.projectcitybuild.pcbridge.paper.runtime.integrations.PaperIntegration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dynmapModule =
    module {
        singleOf(::DynmapIntegration) bind PaperIntegration::class
    }
