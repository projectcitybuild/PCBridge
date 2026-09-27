package com.projectcitybuild.pcbridge.paper.features.workstations

import com.projectcitybuild.pcbridge.paper.features.workstations.commands.AnvilCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.CartographyTableCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.EnchantingCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.GrindstoneCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.LoomCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.SmithingTableCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.StoneCutterCommand
import com.projectcitybuild.pcbridge.paper.features.workstations.commands.WorkbenchCommand
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val workstationsModule =
    module {
        factoryOf(::AnvilCommand)
        factoryOf(::CartographyTableCommand)
        factoryOf(::EnchantingCommand)
        factoryOf(::GrindstoneCommand)
        factoryOf(::LoomCommand)
        factoryOf(::SmithingTableCommand)
        factoryOf(::StoneCutterCommand)
        factoryOf(::WorkbenchCommand)

        paperFeature("workstations") {
            commands(
                get<AnvilCommand>(),
                get<CartographyTableCommand>(),
                get<EnchantingCommand>(),
                get<GrindstoneCommand>(),
                get<LoomCommand>(),
                get<SmithingTableCommand>(),
                get<StoneCutterCommand>(),
                get<WorkbenchCommand>(),
            )
        }
    }
