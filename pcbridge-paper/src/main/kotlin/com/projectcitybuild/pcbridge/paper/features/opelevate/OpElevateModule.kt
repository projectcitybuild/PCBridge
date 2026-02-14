package com.projectcitybuild.pcbridge.paper.features.opelevate

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.repositories.OpElevationRepository
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services.OpElevationScheduler
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.commands.OpEndCommand
import com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.commands.OpMeCommand
import com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener.OpDialogListener
import com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener.OpJoinListener
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val opElevateModule = module {
    factory {
        OpJoinListener(
            plugin = get<JavaPlugin>(),
            server = get(),
            opElevationService = get(),
        )
    }

    factory {
        OpDialogListener(
            opElevationService = get(),
        )
    }

    factory {
        OpMeCommand(
            plugin = get<JavaPlugin>(),
            opElevationService = get(),
        )
    }

    factory {
        OpEndCommand(
            plugin = get<JavaPlugin>(),
            opElevationService = get(),
        )
    }

    factory {
        OpElevationService(
            opElevationRepository = get(),
            scheduler = get(),
            server = get(),
            localizedTime = get(),
        )
    }

    factory {
        OpElevationRepository(
            opElevateHttpService = get<PCBHttp>().opElevate,
            session = get(),
        )
    }

    single {
        OpElevationScheduler(
            timer = get(),
        )
    }
}