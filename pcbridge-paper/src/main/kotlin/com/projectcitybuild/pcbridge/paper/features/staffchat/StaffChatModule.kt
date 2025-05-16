package com.projectcitybuild.pcbridge.paper.features.staffchat

import com.projectcitybuild.pcbridge.paper.features.staffchat.commands.StaffChatCommand
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val staffChatModule = module {
    factory {
        StaffChatCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            remoteConfig = get(),
            decorators = get(),
        )
    }
}