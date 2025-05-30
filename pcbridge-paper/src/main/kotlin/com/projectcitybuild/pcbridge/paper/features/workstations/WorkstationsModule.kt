package com.projectcitybuild.pcbridge.paper.features.workstations

import com.projectcitybuild.pcbridge.paper.features.workstations.commands.AnvilCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.CartographyTableCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.EnchantingCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.WorkbenchCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.GrindstoneCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.LoomCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.SmithingTableCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.StoneCutterCommand
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val workstationsModule = module {
    factory {
        AnvilCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        CartographyTableCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        EnchantingCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        GrindstoneCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        LoomCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        SmithingTableCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        StoneCutterCommand(
            plugin = get<JavaPlugin>(),
        )
    }

    factory {
        WorkbenchCommand(
            plugin = get<JavaPlugin>(),
        )
    }
}