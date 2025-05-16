package com.projectcitybuild.pcbridge.paper.features.register

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.register.commands.CodeCommand
import com.projectcitybuild.pcbridge.paper.features.register.commands.RegisterCommand
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val registerModule = module {
    factory {
        RegisterCommand(
            plugin = get<JavaPlugin>(),
            registerHttpService = get<PCBHttp>().register,
        )
    }
    factory {
        CodeCommand(
            plugin = get<JavaPlugin>(),
            registerHttpService = get<PCBHttp>().register,
        )
    }
}