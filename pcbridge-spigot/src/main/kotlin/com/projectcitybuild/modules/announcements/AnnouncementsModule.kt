package com.projectcitybuild.modules.announcements

import com.projectcitybuild.modules.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class AnnouncementsModule: PluginModule {
    private var action: StartAnnouncementTimer? = null

    override fun register(module: ModuleDeclaration) {
        module {
            action = StartAnnouncementTimer(
                container.scheduledAnnouncementsRepository,
                container.config,
                container.timer,
                container.spigotServer,
            )
            action?.start()
        }
    }

    override fun unregister() {
        action?.stop()
    }
}