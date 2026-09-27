package com.projectcitybuild.pcbridge.paper.features.announcements

import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.paper.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.paper.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val announcementsModule =
    module {
        paperFeature("announcements") {
            listeners(
                get<AnnouncementConfigListener>(),
                get<AnnouncementEnableListener>(),
            )
        }

        singleOf(::AnnouncementRepository)
        singleOf(::StartAnnouncementTimer)
        factoryOf(::AnnouncementEnableListener)
        factoryOf(::AnnouncementConfigListener)
    }
