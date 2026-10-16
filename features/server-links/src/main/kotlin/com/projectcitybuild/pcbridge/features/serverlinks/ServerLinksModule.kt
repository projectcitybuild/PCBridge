package com.projectcitybuild.pcbridge.features.serverlinks

import com.projectcitybuild.pcbridge.features.serverlinks.listeners.ServerLinkListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val serverLinksModule =
    module {
        paperFeature("server-links") {
            listeners(get<ServerLinkListener>())
        }

        factoryOf(::ServerLinkListener)
    }
