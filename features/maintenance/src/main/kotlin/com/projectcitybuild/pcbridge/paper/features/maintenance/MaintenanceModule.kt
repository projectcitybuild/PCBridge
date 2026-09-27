package com.projectcitybuild.pcbridge.paper.features.maintenance

import com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.commands.MaintenanceCommand
import com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.decorators.MaintenanceMotdDecorator
import com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.listener.MaintenanceReminderListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.middleware.MaintenanceConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val maintenanceModule =
    module {
        factoryOf(::MaintenanceConnectionMiddleware)
        factoryOf(::MaintenanceMotdDecorator)
        factoryOf(::MaintenanceReminderListener)
        factoryOf(::MaintenanceCommand)

        paperFeature("maintenance") {
            commands(get<MaintenanceCommand>())
            listeners(get<MaintenanceReminderListener>())
            connectionMiddleware(get<MaintenanceConnectionMiddleware>(), priority = 200)
            serverListingDecorator(get<MaintenanceMotdDecorator>(), priority = 100)
        }
    }
