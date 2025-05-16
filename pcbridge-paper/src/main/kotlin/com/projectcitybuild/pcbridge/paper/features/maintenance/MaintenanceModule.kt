package com.projectcitybuild.pcbridge.paper.features.maintenance

import com.projectcitybuild.pcbridge.paper.features.maintenance.commands.MaintenanceCommand
import com.projectcitybuild.pcbridge.paper.features.maintenance.decorators.MaintenanceMotdDecorator
import com.projectcitybuild.pcbridge.paper.features.maintenance.listener.MaintenanceReminderListener
import com.projectcitybuild.pcbridge.paper.features.maintenance.middleware.MaintenanceConnectionMiddleware
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val maintenanceModule = module {
    factory {
        MaintenanceConnectionMiddleware(
            store = get(),
        )
    }

    factory {
        MaintenanceMotdDecorator(
            store = get(),
        )
    }

    factory {
        MaintenanceReminderListener(
            store = get(),
            server = get(),
            timer = get(),
        )
    }

    factory {
        MaintenanceCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            store = get(),
            eventBroadcaster = get(),
        )
    }
}