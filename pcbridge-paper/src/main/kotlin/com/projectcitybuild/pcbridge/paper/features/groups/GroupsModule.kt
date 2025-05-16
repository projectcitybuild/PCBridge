package com.projectcitybuild.pcbridge.paper.features.groups

import com.projectcitybuild.pcbridge.paper.features.groups.decorators.ChatGroupDecorator
import com.projectcitybuild.pcbridge.paper.features.groups.listener.ChatGroupInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.groups.listener.RoleStateChangeListener
import com.projectcitybuild.pcbridge.paper.features.groups.placeholders.TabGroupListPlaceholder
import com.projectcitybuild.pcbridge.paper.features.groups.placeholders.TabGroupsPlaceholder
import com.projectcitybuild.pcbridge.paper.features.groups.repositories.ChatGroupRepository
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
            store = get(),
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
            store = get(),
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