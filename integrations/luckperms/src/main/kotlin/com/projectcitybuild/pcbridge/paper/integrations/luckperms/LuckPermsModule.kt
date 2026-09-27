package com.projectcitybuild.pcbridge.paper.integrations.luckperms

import com.projectcitybuild.pcbridge.paper.runtime.integrations.PaperIntegration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val luckPermsModule =
    module {
        singleOf(::LuckPermsIntegration) bind PaperIntegration::class
    }
