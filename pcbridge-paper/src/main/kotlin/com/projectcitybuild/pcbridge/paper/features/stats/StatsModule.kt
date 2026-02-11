package com.projectcitybuild.pcbridge.paper.features.stats

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.stats.domain.StatsCollector
import com.projectcitybuild.pcbridge.paper.features.stats.domain.repositories.StatsRepository
import com.projectcitybuild.pcbridge.paper.features.stats.hooks.listeners.BlockChangeListener
import org.koin.dsl.module

val statsModule = module {
    factory {
        BlockChangeListener(
            statsCollector = get(),
        )
    }

    factory {
        StatsRepository(
            statsHttpService = get<PCBHttp>().stats,
        )
    }

    single {
        StatsCollector(
            statsRepository = get(),
        )
    }
}