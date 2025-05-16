package com.projectcitybuild.pcbridge.paper.features.watchdog

import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.ItemTextListener
import org.koin.dsl.module

val watchDogModule = module {
    factory {
        ItemTextListener(
            discordSend = get(),
            time = get(),
        )
    }
}