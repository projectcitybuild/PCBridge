package com.projectcitybuild.pcbridge.paper.features.opelevate

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
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
            session = get(),
        )
    }

    factory {
        OpDialogListener(
            opElevateHttpService = get<PCBHttp>().opElevate,
        )
    }

    factory {
        OpMeCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        OpEndCommand(
            plugin = get<JavaPlugin>(),
            opElevateHttpService = get<PCBHttp>().opElevate,
        )
    }
}