package com.projectcitybuild.pcbridge.paper.features.roles

import com.projectcitybuild.pcbridge.paper.features.roles.hooks.decorators.ChatRoleDecorator
import com.projectcitybuild.pcbridge.paper.features.roles.domain.ChatRoleFormatter
import com.projectcitybuild.pcbridge.paper.features.roles.domain.RolesFilter
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.listener.ChatRoleInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.listener.RoleStateChangeListener
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.placeholders.TabRoleListPlaceholder
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.placeholders.TabRolesPlaceholder
import com.projectcitybuild.pcbridge.paper.features.roles.domain.repositories.ChatRoleRepository
import io.github.reactivecircus.cache4k.Cache
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.UUID

val rolesModule = module {
    factory {
        ChatRoleInvalidateListener(
            chatRoleRepository = get(),
        )
    }

    factory {
        RoleStateChangeListener(
            permissions = get(),
        )
    }

    factory {
        ChatRoleDecorator(
            chatRoleRepository = get(),
        )
    }

    factory {
        TabRoleListPlaceholder(
            rolesFilter = get(),
            session = get(),
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        TabRolesPlaceholder(
            chatRoleRepository = get(),
            server = get(),
            tabRenderer = get(),
        )
    }

    factory {
        ChatRoleRepository(
            chatRoleFormatter = get(),
            session = get(),
            roleCache = get(named("role_cache")),
        )
    }

    single(named("role_cache")) {
        Cache.Builder<UUID, ChatRoleRepository.CachedComponent>().build()
    }

    factory {
        ChatRoleFormatter(
            rolesFilter = get(),
        )
    }

    factory {
        RolesFilter()
    }
}