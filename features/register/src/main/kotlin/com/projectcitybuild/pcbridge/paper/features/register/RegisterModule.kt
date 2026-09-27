package com.projectcitybuild.pcbridge.paper.features.register

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.register.commands.CodeCommand
import com.projectcitybuild.pcbridge.paper.features.register.commands.RegisterCommand
import com.projectcitybuild.pcbridge.paper.features.register.listeners.VerifyCodeDialogListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val registerModule =
    module {
        factory {
            RegisterCommand(
                plugin = get<JavaPlugin>(),
                registerHttpService = get<PCBHttp>().register,
            )
        }

        factoryOf(::CodeCommand)

        factory {
            VerifyCodeDialogListener(
                registerHttpService = get<PCBHttp>().register,
                syncPlayer = get(),
            )
        }

        paperFeature("register") {
            commands(
                get<CodeCommand>(),
                get<RegisterCommand>(),
            )
            listeners(get<VerifyCodeDialogListener>())
        }
    }
