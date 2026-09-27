package com.projectcitybuild.pcbridge.paper.features.building

import com.projectcitybuild.pcbridge.paper.features.building.hooks.commands.InvisFrameCommand
import com.projectcitybuild.pcbridge.paper.features.building.hooks.commands.ItemNameCommand
import com.projectcitybuild.pcbridge.paper.features.building.hooks.commands.NightVisionCommand
import com.projectcitybuild.pcbridge.paper.features.building.hooks.listeners.InvisFrameListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val buildingModule =
    module {
        factoryOf(::NightVisionCommand)
        factoryOf(::ItemNameCommand)
        factoryOf(::InvisFrameCommand)
        factoryOf(::InvisFrameListener)

        paperFeature("building") {
            commands(
                get<InvisFrameCommand>(),
                get<ItemNameCommand>(),
                get<NightVisionCommand>(),
            )
            listeners(get<InvisFrameListener>())
        }
    }
