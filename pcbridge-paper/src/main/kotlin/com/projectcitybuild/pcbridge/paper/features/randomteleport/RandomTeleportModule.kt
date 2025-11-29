package com.projectcitybuild.pcbridge.paper.features.randomteleport

import com.projectcitybuild.pcbridge.paper.features.randomteleport.domain.actions.FindRandomLocation
import com.projectcitybuild.pcbridge.paper.features.randomteleport.hooks.commands.RtpCommand
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val randomTeleportModule = module {
    factory {
        RtpCommand(
            plugin = get<JavaPlugin>(),
            findRandomLocation = get(),
            cooldown = get(),
        )
    }

    factory {
        FindRandomLocation(
            playerTeleporter = get(),
        )
    }
}