package com.projectcitybuild.pcbridge.paper.features.onboarding

import com.projectcitybuild.pcbridge.paper.features.onboarding.hooks.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.paper.features.onboarding.hooks.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.paper.features.onboarding.hooks.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.paper.features.onboarding.hooks.listeners.ServerOverviewJoinListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val onboardingModule =
    module {
        factoryOf(::AnnounceJoinListener)
        factoryOf(::AnnounceQuitListener)
        factoryOf(::FirstTimeJoinListener)
        factoryOf(::ServerOverviewJoinListener)

        paperFeature("onboarding") {
            listeners(
                get<AnnounceJoinListener>(),
                get<AnnounceQuitListener>(),
                get<FirstTimeJoinListener>(),
                get<ServerOverviewJoinListener>(),
            )
        }
    }
