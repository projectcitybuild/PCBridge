package com.projectcitybuild.pcbridge.paper.features.onboarding

import com.projectcitybuild.pcbridge.paper.features.onboarding.hooks.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.paper.features.onboarding.hooks.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.paper.features.onboarding.hooks.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.paper.features.onboarding.hooks.listeners.ServerOverviewJoinListener
import org.koin.dsl.module

val onboardingModule = module {
    factory {
        AnnounceJoinListener(
            remoteConfig = get(),
        )
    }

    factory {
        AnnounceQuitListener(
            remoteConfig = get(),
            session = get(),
            time = get(),
        )
    }

    factory {
        FirstTimeJoinListener(
            remoteConfig = get(),
            server = get(),
            session = get(),
        )
    }

    factory {
        ServerOverviewJoinListener(
            remoteConfig = get(),
        )
    }
}