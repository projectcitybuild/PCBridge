package com.projectcitybuild.pcbridge.paper.features.building

import com.projectcitybuild.pcbridge.paper.features.building.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.paper.features.building.commands.ItemNameCommand
import com.projectcitybuild.pcbridge.paper.features.building.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.paper.features.building.listeners.InvisFrameListener
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val buildingModule = module {
    factory {
        NightVisionCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        ItemNameCommand(
            plugin = get<JavaPlugin>(),
            eventBroadcaster = get(),
        )
    }

    factory {
        InvisFrameCommand(
            plugin = get<JavaPlugin>(),
            spigotNamespace = get(),
        )
    }

    factory {
        InvisFrameListener(
            spigotNamespace = get(),
        )
    }
}