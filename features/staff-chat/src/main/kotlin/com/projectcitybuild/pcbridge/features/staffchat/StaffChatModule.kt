package com.projectcitybuild.pcbridge.features.staffchat

import com.projectcitybuild.pcbridge.features.staffchat.commands.StaffChatCommand
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val staffChatModule =
    module {
        paperFeature("staff-chat") {
            commands(get<StaffChatCommand>())
        }

        factoryOf(::StaffChatCommand)
    }
