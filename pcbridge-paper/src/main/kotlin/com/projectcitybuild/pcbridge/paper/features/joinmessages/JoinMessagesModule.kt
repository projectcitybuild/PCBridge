package com.projectcitybuild.pcbridge.paper.features.joinmessages

import com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners.AnnounceJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners.AnnounceQuitListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners.FirstTimeJoinListener
import com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners.ServerOverviewJoinListener
import org.koin.dsl.module

val joinMessagesModule = module {
    factory {
        AnnounceJoinListener(
            remoteConfig = get(),
        )
    }

    factory {
        AnnounceQuitListener(
            remoteConfig = get(),
            store = get(),
            time = get(),
        )
    }

    factory {
        FirstTimeJoinListener(
            remoteConfig = get(),
            server = get(),
            store = get(),
        )
    }

    factory {
        ServerOverviewJoinListener(
            remoteConfig = get(),
        )
    }
}