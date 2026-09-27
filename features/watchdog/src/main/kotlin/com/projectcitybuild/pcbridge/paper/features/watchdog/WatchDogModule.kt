package com.projectcitybuild.pcbridge.paper.features.watchdog

import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.ItemTextListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val watchDogModule =
    module {
        factoryOf(::ItemTextListener)

        paperFeature("watchdog") {
            listeners(get<ItemTextListener>())
        }
    }
