package com.projectcitybuild.pcbridge.paper.features.pim

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.pim.domain.repositories.OpElevationRepository
import com.projectcitybuild.pcbridge.paper.features.pim.domain.services.OpElevationScheduler
import com.projectcitybuild.pcbridge.paper.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.PimCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.OpClearListener
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.OpDialogListener
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.OpRestoreListener
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.VanillaOpInterceptListener
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val pimModule = module {
    factory {
        OpRestoreListener(
            opElevationService = get(),
        )
    }

    factory {
        OpClearListener(
            plugin = get<JavaPlugin>(),
            server = get(),
        )
    }

    factory {
        OpDialogListener(
            opElevationService = get(),
        )
    }

    factory {
        VanillaOpInterceptListener()
    }

    factory {
        PimCommand(
            opGrantCommand = get(),
            opRevokeCommand = get(),
            opStatusCommand = get(),
            rolesDebugCommand = get(),
        )
    }

    factory {
        OpGrantCommand(
            plugin = get<JavaPlugin>(),
            opElevationService = get(),
            localizedTime = get(),
        )
    }

    factory {
        OpStatusCommand(
            plugin = get<JavaPlugin>(),
            opElevationService = get(),
            localizedTime = get(),
        )
    }

    factory {
        OpRevokeCommand(
            plugin = get<JavaPlugin>(),
            opElevationService = get(),
        )
    }

    factory {
        RolesDebugCommand(
            plugin = get<JavaPlugin>(),
            permissions = get(),
        )
    }

    factory {
        OpElevationService(
            plugin = get<JavaPlugin>(),
            opElevationRepository = get(),
            opElevationScheduler = get(),
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