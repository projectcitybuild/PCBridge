package com.projectcitybuild.pcbridge.paper.features.roles

import com.projectcitybuild.pcbridge.paper.features.roles.domain.ChatRoleFormatter
import com.projectcitybuild.pcbridge.paper.features.roles.domain.RolesFilter
import com.projectcitybuild.pcbridge.paper.features.roles.domain.repositories.ChatRoleRepository
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.decorators.ChatRoleDecorator
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.listener.ChatRoleInvalidateListener
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.listener.RoleStateChangeListener
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.placeholders.TabRoleListPlaceholder
import com.projectcitybuild.pcbridge.paper.features.roles.hooks.placeholders.TabRolesPlaceholder
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import io.github.reactivecircus.cache4k.Cache
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.UUID

val rolesModule =
    module {
        paperFeature("roles") {
            listeners(
                get<ChatRoleInvalidateListener>(),
                get<RoleStateChangeListener>(),
            )
            chatSenderDecorator(get<ChatRoleDecorator>(), priority = 100)
            tabSectionPlaceholder(get<TabRoleListPlaceholder>(), priority = 400)
            tabPlayerPlaceholder(get<TabRolesPlaceholder>(), priority = 400)
        }

        factoryOf(::ChatRoleInvalidateListener)
        factoryOf(::RoleStateChangeListener)
        factoryOf(::ChatRoleDecorator)
        factoryOf(::TabRoleListPlaceholder)
        factoryOf(::TabRolesPlaceholder)

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

        factoryOf(::ChatRoleFormatter)
        factoryOf(::RolesFilter)
    }
