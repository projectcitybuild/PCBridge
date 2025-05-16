package com.projectcitybuild.pcbridge.paper.features.serverlinks

import com.projectcitybuild.pcbridge.paper.features.serverlinks.listeners.ServerLinkListener
import org.koin.dsl.module

val serverLinksModule = module {
    factory {
        ServerLinkListener(
            remoteConfig = get(),
        )
    }
}