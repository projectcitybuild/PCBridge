package com.projectcitybuild.pcbridge.paper.features.chatbadge

import com.projectcitybuild.pcbridge.paper.features.chatbadge.domain.ChatBadgeFormatter
import com.projectcitybuild.pcbridge.paper.features.chatbadge.domain.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.chatbadge.hooks.decorators.ChatBadgeDecorator
import com.projectcitybuild.pcbridge.paper.features.chatbadge.hooks.listeners.ChatBadgeInvalidateListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import io.github.reactivecircus.cache4k.Cache
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.UUID

val chatBadgeModule =
    module {
        paperFeature("chat-badge") {
            listeners(get<ChatBadgeInvalidateListener>())
            chatSenderDecorator(get<ChatBadgeDecorator>(), priority = 200)
        }

        factoryOf(::ChatBadgeInvalidateListener)

        factory {
            ChatBadgeRepository(
                session = get(),
                remoteConfig = get(),
                badgeFormatter = get(),
                badgeCache = get(named("badge_cache")),
            )
        }

        factoryOf(::ChatBadgeFormatter)

        single(named("badge_cache")) {
            Cache.Builder<UUID, ChatBadgeRepository.CachedComponent>().build()
        }

        factoryOf(::ChatBadgeDecorator)
    }
