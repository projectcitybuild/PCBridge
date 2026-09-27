package com.projectcitybuild.pcbridge.paper.runtime.features

import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.registerCommands
import com.projectcitybuild.pcbridge.paper.platform.paper.listeners.SpigotListenerRegistry
import com.projectcitybuild.pcbridge.paper.runtime.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.runtime.chat.decorators.ChatMessageDecorator
import com.projectcitybuild.pcbridge.paper.runtime.chat.decorators.ChatSenderDecorator
import com.projectcitybuild.pcbridge.paper.runtime.connection.middleware.ConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.runtime.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.runtime.serverlist.decorators.ServerListingDecorator
import com.projectcitybuild.pcbridge.paper.runtime.serverlist.decorators.ServerListingDecoratorChain
import com.projectcitybuild.pcbridge.paper.runtime.tablist.TabPlaceholder
import com.projectcitybuild.pcbridge.paper.runtime.tablist.TabPlaceholders
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import kotlin.reflect.KClass

data class OrderedHook<T>(
    val value: T,
    val priority: Int,
    val sequence: Int,
)

class PaperFeatureRegistration internal constructor(
    val name: String,
    val commands: List<OrderedHook<BrigadierCommand>>,
    val listeners: List<OrderedHook<Listener>>,
    val connectionMiddleware: List<OrderedHook<ConnectionMiddleware>>,
    val chatSenderDecorators: List<OrderedHook<ChatSenderDecorator>>,
    val chatMessageDecorators: List<OrderedHook<ChatMessageDecorator>>,
    val serverListingDecorators: List<OrderedHook<ServerListingDecorator>>,
    val tabSectionPlaceholders: List<OrderedHook<TabPlaceholder>>,
    val tabPlayerPlaceholders: List<OrderedHook<TabPlaceholder>>,
)

class PaperFeatureRegistrationBuilder internal constructor(
    private val name: String,
    private val scope: Scope,
) {
    private var sequence = 0
    private val commands = mutableListOf<OrderedHook<BrigadierCommand>>()
    private val listeners = mutableListOf<OrderedHook<Listener>>()
    private val connectionMiddleware = mutableListOf<OrderedHook<ConnectionMiddleware>>()
    private val chatSenderDecorators = mutableListOf<OrderedHook<ChatSenderDecorator>>()
    private val chatMessageDecorators = mutableListOf<OrderedHook<ChatMessageDecorator>>()
    private val serverListingDecorators = mutableListOf<OrderedHook<ServerListingDecorator>>()
    private val tabSectionPlaceholders = mutableListOf<OrderedHook<TabPlaceholder>>()
    private val tabPlayerPlaceholders = mutableListOf<OrderedHook<TabPlaceholder>>()

    inline fun <reified T : Any> get(): T = resolve(T::class)

    fun commands(vararg commands: BrigadierCommand) = commands.forEach { this.commands.add(it.ordered()) }

    fun listeners(vararg listeners: Listener) = listeners.forEach { this.listeners.add(it.ordered()) }

    fun connectionMiddleware(
        middleware: ConnectionMiddleware,
        priority: Int,
    ) = connectionMiddleware.add(middleware.ordered(priority))

    fun chatSenderDecorator(
        decorator: ChatSenderDecorator,
        priority: Int,
    ) = chatSenderDecorators.add(decorator.ordered(priority))

    fun chatMessageDecorator(
        decorator: ChatMessageDecorator,
        priority: Int,
    ) = chatMessageDecorators.add(decorator.ordered(priority))

    fun serverListingDecorator(
        decorator: ServerListingDecorator,
        priority: Int,
    ) = serverListingDecorators.add(decorator.ordered(priority))

    fun tabSectionPlaceholder(
        placeholder: TabPlaceholder,
        priority: Int,
    ) = tabSectionPlaceholders.add(placeholder.ordered(priority))

    fun tabPlayerPlaceholder(
        placeholder: TabPlaceholder,
        priority: Int,
    ) = tabPlayerPlaceholders.add(placeholder.ordered(priority))

    internal fun build() =
        PaperFeatureRegistration(
            name = name,
            commands = commands,
            listeners = listeners,
            connectionMiddleware = connectionMiddleware,
            chatSenderDecorators = chatSenderDecorators,
            chatMessageDecorators = chatMessageDecorators,
            serverListingDecorators = serverListingDecorators,
            tabSectionPlaceholders = tabSectionPlaceholders,
            tabPlayerPlaceholders = tabPlayerPlaceholders,
        )

    @PublishedApi
    internal fun <T : Any> resolve(type: KClass<T>): T = scope.get(type)

    private fun <T> T.ordered(priority: Int = 0) =
        OrderedHook(
            value = this,
            priority = priority,
            sequence = sequence++,
        )
}

fun Module.paperFeature(
    name: String,
    registration: PaperFeatureRegistrationBuilder.() -> Unit,
) {
    single(named("paper-feature:$name")) {
        PaperFeatureRegistrationBuilder(name, this)
            .apply(registration)
            .build()
    } bind PaperFeatureRegistration::class
}

class PaperFeatureRegistrar(
    private val plugin: JavaPlugin,
    private val listenerRegistry: SpigotListenerRegistry,
    private val connectionMiddlewareChain: ConnectionMiddlewareChain,
    private val chatDecoratorChain: ChatDecoratorChain,
    private val serverListingDecoratorChain: ServerListingDecoratorChain,
    private val tabPlaceholders: TabPlaceholders,
) {
    fun register(registrations: List<PaperFeatureRegistration>) {
        plugin.registerCommands(*registrations.ordered { commands }.toTypedArray())
        listenerRegistry.register(*registrations.ordered { listeners }.toTypedArray())
        connectionMiddlewareChain.register(*registrations.ordered { connectionMiddleware }.toTypedArray())
        chatDecoratorChain.senders(*registrations.ordered { chatSenderDecorators }.toTypedArray())
        chatDecoratorChain.messages(*registrations.ordered { chatMessageDecorators }.toTypedArray())
        serverListingDecoratorChain.register(*registrations.ordered { serverListingDecorators }.toTypedArray())
        tabPlaceholders.sections(*registrations.ordered { tabSectionPlaceholders }.toTypedArray())
        tabPlaceholders.players(*registrations.ordered { tabPlayerPlaceholders }.toTypedArray())
    }

    private fun <T> List<PaperFeatureRegistration>.ordered(select: PaperFeatureRegistration.() -> List<OrderedHook<T>>): List<T> =
        flatMap { registration ->
            registration.select().map { hook -> Triple(hook, registration.name, hook.sequence) }
        }.sortedWith(
            compareBy<Triple<OrderedHook<T>, String, Int>>(
                { it.first.priority },
                { it.second },
                { it.third },
            ),
        ).map { it.first.value }
}
