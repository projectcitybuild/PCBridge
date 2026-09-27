package com.projectcitybuild.pcbridge.paper.runtime

import com.projectcitybuild.pcbridge.paper.runtime.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.runtime.chat.listeners.AsyncChatListener
import com.projectcitybuild.pcbridge.paper.runtime.connection.listeners.AuthorizeConnectionListener
import com.projectcitybuild.pcbridge.paper.runtime.connection.listeners.EndConnectionListener
import com.projectcitybuild.pcbridge.paper.runtime.connection.listeners.PlayerStateListener
import com.projectcitybuild.pcbridge.paper.runtime.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.runtime.exceptions.listeners.CoroutineExceptionListener
import com.projectcitybuild.pcbridge.paper.runtime.features.PaperFeatureRegistrar
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import com.projectcitybuild.pcbridge.paper.runtime.integrations.PaperIntegrationRegistrar
import com.projectcitybuild.pcbridge.paper.runtime.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.runtime.serverlist.decorators.ServerListingDecoratorChain
import com.projectcitybuild.pcbridge.paper.runtime.serverlist.listeners.ServerListPingListener
import com.projectcitybuild.pcbridge.paper.runtime.tablist.TabPlaceholders
import com.projectcitybuild.pcbridge.paper.runtime.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.runtime.tablist.listeners.TabListeners
import com.projectcitybuild.pcbridge.paper.runtime.tablist.placeholders.MaxPlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.runtime.tablist.placeholders.OnlinePlayerCountPlaceholder
import com.projectcitybuild.pcbridge.paper.runtime.tablist.placeholders.PlayerAFKPlaceholder
import com.projectcitybuild.pcbridge.paper.runtime.tablist.placeholders.PlayerNamePlaceholder
import com.projectcitybuild.pcbridge.paper.runtime.tablist.placeholders.PlayerPingPlaceholder
import com.projectcitybuild.pcbridge.paper.runtime.tablist.placeholders.PlayerWorldPlaceholder
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val runtimeModule =
    module {
        single { ConnectionMiddlewareChain() }
        single { ChatDecoratorChain() }
        single { ServerListingDecoratorChain() }
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
