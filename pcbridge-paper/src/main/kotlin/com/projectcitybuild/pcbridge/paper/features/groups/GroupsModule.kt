package com.projectcitybuild.pcbridge.paper.features.groups

import com.projectcitybuild.pcbridge.paper.features.groups.hooks.decorators.ChatGroupDecorator
import com.projectcitybuild.pcbridge.paper.features.groups.domain.ChatGroupFormatter
import com.projectcitybuild.pcbridge.paper.features.groups.domain.RolesFilter
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.listener.ChatGroupInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.listener.RoleStateChangeListener
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.placeholders.TabGroupListPlaceholder
import com.projectcitybuild.pcbridge.paper.features.groups.hooks.placeholders.TabGroupsPlaceholder
import com.projectcitybuild.pcbridge.paper.features.groups.domain.repositories.ChatGroupRepository
import io.github.reactivecircus.cache4k.Cache
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.UUID

val groupsModule = module {
    factory {
        ChatGroupInvalidateListener(
            chatGroupRepository = get(),
        )
    }

    factory {
        RoleStateChangeListener(
            permissions = get(),
        )
    }

    factory {
        ChatGroupDecorator(
            chatGroupRepository = get(),
        )
    }

    factory {
        TabGroupListPlaceholder(
            rolesFilter = get(),
            session = get(),
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        TabGroupsPlaceholder(
            chatGroupRepository = get(),
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        ChatGroupRepository(
            chatGroupFormatter = get(),
            session = get(),
            groupCache = get(named("group_cache")),
        )
    }

    single(named("group_cache")) {
        Cache.Builder<UUID, ChatGroupRepository.CachedComponent>().build()
    }

    factory {
        ChatGroupFormatter(
            rolesFilter = get(),
        )
    }

    factory {
        RolesFilter()
    }
}