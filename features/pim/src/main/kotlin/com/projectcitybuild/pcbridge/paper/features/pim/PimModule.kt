package com.projectcitybuild.pcbridge.paper.features.pim

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.pim.domain.repositories.OpAuditRepository
import com.projectcitybuild.pcbridge.paper.features.pim.domain.repositories.OpElevationRepository
import com.projectcitybuild.pcbridge.paper.features.pim.domain.services.OpElevationScheduler
import com.projectcitybuild.pcbridge.paper.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.PimCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.OpAuditingListener
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.OpClearListener
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.OpDialogListener
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.OpRestoreListener
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener.VanillaOpInterceptListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val pimModule =
    module {
        paperFeature("pim") {
            commands(get<PimCommand>())
            listeners(
                get<OpAuditingListener>(),
                get<OpClearListener>(),
                get<OpDialogListener>(),
                get<OpRestoreListener>(),
                get<VanillaOpInterceptListener>(),
            )
        }

        factoryOf(::OpRestoreListener)
        factoryOf(::OpClearListener)
        factoryOf(::OpDialogListener)
        factoryOf(::VanillaOpInterceptListener)
        factoryOf(::OpAuditingListener)
        factoryOf(::PimCommand)
        factoryOf(::OpGrantCommand)
        factoryOf(::OpStatusCommand)
        factoryOf(::OpRevokeCommand)
        factoryOf(::RolesDebugCommand)
        factoryOf(::OpElevationService)

        factory {
            OpElevationRepository(
                opElevateHttpService = get<PCBHttp>().opElevate,
                session = get(),
            )
        }

        factory {
            OpAuditRepository(
                server = get(),
                opElevateHttpService = get<PCBHttp>().opElevate,
            )
        }

        singleOf(::OpElevationScheduler)
    }
