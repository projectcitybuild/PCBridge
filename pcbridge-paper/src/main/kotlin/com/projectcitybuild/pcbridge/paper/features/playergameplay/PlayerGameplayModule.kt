package com.projectcitybuild.pcbridge.paper.features.playergameplay

import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.BurnCommand
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.ExtinguishCommand
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.FeedCommand
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.GameModeCommand
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.HealCommand
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.IceCommand
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.KillCommand
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.PurgeCommand
import com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.SuicideCommand
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val playerGameplayModule = module {
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
        GameModeCommand(
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

    factory {
        SuicideCommand(
            plugin = get<JavaPlugin>(),
        )
    }
}