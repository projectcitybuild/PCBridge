package com.projectcitybuild.pcbridge.features.maintenance

import com.projectcitybuild.pcbridge.features.maintenance.hooks.commands.MaintenanceCommand
import com.projectcitybuild.pcbridge.features.maintenance.hooks.decorators.MaintenanceMotdDecorator
import com.projectcitybuild.pcbridge.features.maintenance.hooks.listener.MaintenanceReminderListener
import com.projectcitybuild.pcbridge.features.maintenance.hooks.middleware.MaintenanceConnectionMiddleware
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val maintenanceModule =
    module {
        paperFeature("maintenance") {
            commands(get<MaintenanceCommand>())
            listeners(get<MaintenanceReminderListener>())
            connectionMiddleware(get<MaintenanceConnectionMiddleware>(), priority = 200)
            serverListingDecorator(get<MaintenanceMotdDecorator>(), priority = 100)
        }

        factoryOf(::MaintenanceConnectionMiddleware)
        factoryOf(::MaintenanceMotdDecorator)
        factoryOf(::MaintenanceReminderListener)
        factoryOf(::MaintenanceCommand)
    }
