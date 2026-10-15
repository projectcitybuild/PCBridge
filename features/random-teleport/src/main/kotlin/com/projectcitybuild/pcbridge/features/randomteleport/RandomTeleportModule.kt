package com.projectcitybuild.pcbridge.features.randomteleport

import com.projectcitybuild.pcbridge.features.randomteleport.domain.actions.FindRandomLocation
import com.projectcitybuild.pcbridge.features.randomteleport.hooks.commands.RtpCommand
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val randomTeleportModule =
    module {
        paperFeature("random-teleport") {
            commands(get<RtpCommand>())
        }

        factoryOf(::RtpCommand)
        factoryOf(::FindRandomLocation)
    }
