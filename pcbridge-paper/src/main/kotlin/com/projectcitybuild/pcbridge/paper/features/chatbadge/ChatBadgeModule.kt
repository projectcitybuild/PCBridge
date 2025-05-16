package com.projectcitybuild.pcbridge.paper.features.chatbadge

import com.projectcitybuild.pcbridge.paper.features.chatbadge.decorators.ChatBadgeDecorator
import com.projectcitybuild.pcbridge.paper.features.chatbadge.listeners.ChatBadgeInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.chatbadge.repositories.ChatBadgeRepository
import io.github.reactivecircus.cache4k.Cache
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.UUID

val chatBadgeModule = module {
    factory {
        ChatBadgeInvalidateListener(
            chatBadgeRepository = get(),
        )
    }

    factory {
        ChatBadgeRepository(
            store = get(),
            remoteConfig = get(),
            badgeFormatter = get(),
            badgeCache = get(named("badge_cache")),
        )
    }

    factory {
        ChatBadgeFormatter()
    }

    single(named("badge_cache")) {
        Cache.Builder<UUID, ChatBadgeRepository.CachedComponent>().build()
    }

    factory {
        ChatBadgeDecorator(
            chatBadgeRepository = get(),
        )
    }
}