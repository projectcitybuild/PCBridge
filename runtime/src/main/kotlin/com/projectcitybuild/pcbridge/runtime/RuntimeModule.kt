package com.projectcitybuild.pcbridge.runtime

import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.runtime.chat.listeners.AsyncChatListener
import com.projectcitybuild.pcbridge.runtime.connection.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.runtime.connection.listeners.EndConnectionListener
import com.projectcitybuild.pcbridge.runtime.connection.listeners.PlayerStateListener
import com.projectcitybuild.pcbridge.runtime.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.runtime.exceptions.listeners.CoroutineExceptionListener
import com.projectcitybuild.pcbridge.runtime.features.PaperFeatureRegistrar
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegrationRegistrar
import com.projectcitybuild.pcbridge.runtime.permissions.Permissions
import com.projectcitybuild.pcbridge.runtime.serverlist.decorators.ServerListingDecoratorChain
import com.projectcitybuild.pcbridge.runtime.serverlist.listeners.ServerListPingListener
import com.projectcitybuild.pcbridge.runtime.tablist.TabPlaceholders
import com.projectcitybuild.pcbridge.runtime.tablist.TabRenderer
import com.projectcitybuild.pcbridge.runtime.tablist.listeners.TabListeners
import com.projectcitybuild.pcbridge.runtime.tablist.placeholders.MaxPlayerCountPlaceholder
import com.projectcitybuild.pcbridge.runtime.tablist.placeholders.OnlinePlayerCountPlaceholder
import com.projectcitybuild.pcbridge.runtime.tablist.placeholders.PlayerAFKPlaceholder
import com.projectcitybuild.pcbridge.runtime.tablist.placeholders.PlayerNamePlaceholder
import com.projectcitybuild.pcbridge.runtime.tablist.placeholders.PlayerPingPlaceholder
import com.projectcitybuild.pcbridge.runtime.tablist.placeholders.PlayerWorldPlaceholder
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val runtimeModule =
    module {
        single { ConnectionMiddlewareChain() }
        single { ChatDecoratorChain() }
        single { ServerListingDecoratorChain() }
        single { TabPlaceholders(listenerRegistry = get()) }
        singleOf(::Permissions)
        singleOf(::TabRenderer)
        singleOf(::PaperFeatureRegistrar)
        singleOf(::PaperIntegrationRegistrar)

        factoryOf(::PlayerStateListener)
        factoryOf(::CoroutineExceptionListener)
        factoryOf(::AuthorizeConnectionListener)
        factoryOf(::EndConnectionListener)
        factoryOf(::AsyncChatListener)
        factoryOf(::ServerListPingListener)
        factoryOf(::TabListeners)
        factoryOf(::MaxPlayerCountPlaceholder)
        factoryOf(::OnlinePlayerCountPlaceholder)
        factoryOf(::PlayerAFKPlaceholder)
        factoryOf(::PlayerNamePlaceholder)
        factoryOf(::PlayerPingPlaceholder)
        factoryOf(::PlayerWorldPlaceholder)

        paperFeature("runtime") {
            listeners(
                get<AsyncChatListener>(),
                get<AuthorizeConnectionListener>(),
                get<CoroutineExceptionListener>(),
                get<EndConnectionListener>(),
                get<PlayerStateListener>(),
                get<ServerListPingListener>(),
                get<TabListeners>(),
            )
            tabSectionPlaceholder(get<OnlinePlayerCountPlaceholder>(), priority = 100)
            tabSectionPlaceholder(get<MaxPlayerCountPlaceholder>(), priority = 200)
            tabSectionPlaceholder(get<PlayerWorldPlaceholder>(), priority = 300)
            tabPlayerPlaceholder(get<PlayerNamePlaceholder>(), priority = 100)
            tabPlayerPlaceholder(get<PlayerAFKPlaceholder>(), priority = 200)
            tabPlayerPlaceholder(get<PlayerPingPlaceholder>(), priority = 300)
        }
    }
