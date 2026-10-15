package com.projectcitybuild.pcbridge.features.stats

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.PCBHttp
import com.projectcitybuild.pcbridge.features.stats.domain.StatsCollector
import com.projectcitybuild.pcbridge.features.stats.domain.repositories.StatsRepository
import com.projectcitybuild.pcbridge.features.stats.hooks.listeners.AfkChangeListener
import com.projectcitybuild.pcbridge.features.stats.hooks.listeners.BlockChangeListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val statsModule =
    module {
        paperFeature("stats") {
            listeners(
                get<AfkChangeListener>(),
                get<BlockChangeListener>(),
            )
        }

        factoryOf(::BlockChangeListener)
        factoryOf(::AfkChangeListener)

        factory {
            StatsRepository(
                statsHttpService = get<PCBHttp>().stats,
            )
        }

        singleOf(::StatsCollector)
    }
