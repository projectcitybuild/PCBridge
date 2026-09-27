package com.projectcitybuild.pcbridge.paper.features.stats

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.stats.domain.StatsCollector
import com.projectcitybuild.pcbridge.paper.features.stats.domain.repositories.StatsRepository
import com.projectcitybuild.pcbridge.paper.features.stats.hooks.listeners.AfkChangeListener
import com.projectcitybuild.pcbridge.paper.features.stats.hooks.listeners.BlockChangeListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val statsModule =
    module {
        factoryOf(::BlockChangeListener)
        factoryOf(::AfkChangeListener)

        factory {
            StatsRepository(
                statsHttpService = get<PCBHttp>().stats,
            )
        }

        singleOf(::StatsCollector)

        paperFeature("stats") {
            listeners(
                get<AfkChangeListener>(),
                get<BlockChangeListener>(),
            )
        }
    }
