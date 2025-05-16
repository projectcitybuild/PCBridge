package com.projectcitybuild.pcbridge.paper.features.telemetry

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.telemetry.listeners.TelemetryPlayerConnectListener
import com.projectcitybuild.pcbridge.paper.features.telemetry.repositories.TelemetryRepository
import org.koin.dsl.module

val telemetryModule = module {
    factory {
        TelemetryRepository(
            telemetryHttpService = get<PCBHttp>().telemetry,
        )
    }

    factory {
        TelemetryPlayerConnectListener(
            telemetryRepository = get(),
        )
    }
}