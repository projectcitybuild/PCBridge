package com.projectcitybuild.pcbridge.paper.features.randomteleport

import com.projectcitybuild.pcbridge.paper.features.randomteleport.domain.actions.FindRandomLocation
import com.projectcitybuild.pcbridge.paper.features.randomteleport.hooks.commands.RtpCommand
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val randomTeleportModule =
    module {
        factoryOf(::RtpCommand)
        factoryOf(::FindRandomLocation)

        paperFeature("random-teleport") {
            commands(get<RtpCommand>())
        }
    }
