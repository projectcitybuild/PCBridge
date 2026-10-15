package com.projectcitybuild.pcbridge.paper.features.staffchat

import com.projectcitybuild.pcbridge.paper.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val staffChatModule =
    module {
        paperFeature("staff-chat") {
            commands(get<StaffChatCommand>())
        }

        factoryOf(::StaffChatCommand)
    }
