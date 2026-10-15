package com.projectcitybuild.pcbridge.features.announcements

import com.projectcitybuild.pcbridge.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.features.announcements.listeners.AnnouncementConfigListener
import com.projectcitybuild.pcbridge.features.announcements.listeners.AnnouncementEnableListener
import com.projectcitybuild.pcbridge.features.announcements.repositories.AnnouncementRepository
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
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
