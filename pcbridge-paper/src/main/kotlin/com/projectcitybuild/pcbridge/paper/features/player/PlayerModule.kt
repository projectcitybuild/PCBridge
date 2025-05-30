package com.projectcitybuild.pcbridge.paper.features.player

import com.projectcitybuild.pcbridge.paper.features.player.commands.BurnCommand
import com.projectcitybuild.pcbridge.paper.features.player.commands.ExtinguishCommand
import com.projectcitybuild.pcbridge.paper.features.player.commands.FeedCommand
import com.projectcitybuild.pcbridge.paper.features.player.commands.HealCommand
import com.projectcitybuild.pcbridge.paper.features.player.commands.IceCommand
import com.projectcitybuild.pcbridge.paper.features.player.commands.KillCommand
import com.projectcitybuild.pcbridge.paper.features.player.commands.PurgeCommand
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val playerModule = module {
    factory {
        BurnCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        ExtinguishCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        FeedCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        HealCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        IceCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        KillCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        PurgeCommand(
            plugin = get<JavaPlugin>(),
        )
    }
}