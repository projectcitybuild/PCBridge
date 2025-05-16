package com.projectcitybuild.pcbridge.paper.features.announcements

import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.announcements.repositories.AnnouncementRepository
import org.koin.dsl.module

val announcementsModule = module {
    single {
        AnnouncementRepository(
            remoteConfig = get(),
            store = get(),
        )
    }

    single {
        StartAnnouncementTimer(
            repository = get(),
            remoteConfig = get(),
            timer = get(),
            server = get(),
        )
    }

    factory {
        AnnouncementEnableListener(
            announcementTimer = get(),
            plugin = get(),
        )
    }

    factory {
        AnnouncementConfigListener(
            announcementTimer = get(),
        )
    }
}