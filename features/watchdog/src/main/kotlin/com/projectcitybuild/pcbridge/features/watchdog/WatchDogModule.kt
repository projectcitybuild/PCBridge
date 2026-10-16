package com.projectcitybuild.pcbridge.features.watchdog

import com.projectcitybuild.pcbridge.features.watchdog.listeners.ItemTextListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val watchDogModule =
    module {
        paperFeature("watchdog") {
            listeners(get<ItemTextListener>())
        }

        factoryOf(::ItemTextListener)
    }
