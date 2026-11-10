package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldLifecycleService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.suggestsSuspending
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class WorldLifecycleCommand(
    private val plugin: Plugin,
    private val lifecycleService: WorldLifecycleService,
) : BrigadierCommand {
    private val worldName = Argument.string("world")
    private val sourceName = Argument.string("source")
    private val targetName = Argument.string("target")
    private val environment = Argument.enumValue("environment", World.Environment::class.java)
    private val seed = Argument.string("seed")
    private val worldNameSuggester = WorldNameSuggester(lifecycleService)

    override val builder = command("manage") {
        description = l10n.worlds.worldLifecycleCommandDescription
        requiresPermission(WorldsPermissions.LIFECYCLE)
        literal("list") {
            executesSuspending(plugin) { context ->
                try {
                    val worlds = lifecycleService
                        .list()
                        .map { registration ->
                            val definition = registration.definition
                            val name = "${definition.alias?.takeIf(String::isNotBlank)?.let { "$it/" } ?: ""}${definition.name}"
                            l10n.worlds.worldListEntry(name, registration.isLoaded)
                        }

                    context.source.sender.sendMessage(l10n.worlds.worldList(worlds))
                } catch (failure: Exception) {
                    plugin.logger.log(Level.SEVERE, "Could not read registered world definitions", failure)
                    context.source.sender.sendMessage(l10n.worlds.worldOperationFailed("check the server log for details"))
                }
            }
        }
        literal("import") {
            argument(worldName) {
                executesSuspending(plugin) { context ->
                    runCatchingCommonErrors(context.source.sender, "imported", worldName.get(context)) {
                        lifecycleService.importLoadedWorld(worldName.get(context))
                    }
                }
            }
        }
        literal("load") {
            argument(worldName) {
                suggestsSuspending(plugin) { _, suggestions ->
                    worldNameSuggester.suggestLoadable(suggestions)
                }
                executesSuspending(plugin) { context ->
                    runCatchingCommonErrors(context.source.sender, "loaded", worldName.get(context)) {
                        lifecycleService.load(worldName.get(context))
                    }
                }
            }
        }
        literal("unload") {
            argument(worldName) {
                executesSuspending(plugin) { context ->
                    runCatchingCommonErrors(context.source.sender, "unloaded", worldName.get(context)) {
                        lifecycleService.unload(worldName.get(context))
                    }
                }
            }
        }
        literal("create") {
            argument(worldName) {
                argument(environment) {
                    argument(seed) {
                        executesSuspending(plugin) { context ->
                            val name = worldName.get(context)
                            val worldEnvironment = environment.get(context)
                            val worldSeed = seed.get(context).toLongOrNull()
                            if (worldSeed == null) {
                                context.source.sender.sendMessage(l10n.worlds.worldOperationFailed("seed must be a whole number"))
                            } else {
                                runCatchingCommonErrors(context.source.sender, "created", name) {
                                    lifecycleService.create(
                                        worldDefinition(
                                            name,
                                            worldEnvironment,
                                            worldSeed,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        literal("clone") {
            argument(sourceName) {
                argument(targetName) {
                    literal("confirm") {
                        executesSuspending(plugin) { context ->
                            val target = targetName.get(context)
                            runCatchingCommonErrors(context.source.sender, "cloned", target) {
                                lifecycleService.clone(sourceName.get(context), target, confirmed = true)
                            }
                        }
                    }
                }
            }
        }
        literal("regenerate") {
            requiresPermission(WorldsPermissions.DELETE)
            argument(worldName) {
                literal("confirm") {
                    executesSuspending(plugin) { context ->
                        val name = worldName.get(context)
                        runCatchingCommonErrors(context.source.sender, "regenerated", name) {
                            lifecycleService.regenerate(name, confirmed = true)
                        }
                    }
                }
            }
        }
        literal("unregister") {
            argument(worldName) {
                executesSuspending(plugin) { context ->
                    runCatchingCommonErrors(context.source.sender, "unregistered", worldName.get(context)) {
                        lifecycleService.unregister(worldName.get(context))
                    }
                }
            }
        }
        literal("delete") {
            requiresPermission(WorldsPermissions.DELETE)
            argument(worldName) {
                literal("confirm") {
                    executesSuspending(plugin) { context ->
                        val name = worldName.get(context)
                        runCatchingCommonErrors(context.source.sender, "deleted", name) {
                            lifecycleService.delete(name, confirmed = true)
                        }
                    }
                }
            }
        }
    }

    private inline fun <T> runCatchingCommonErrors(
        sender: CommandSender,
        operation: String,
        name: String,
        action: () -> T,
    ) {
        try {
            action()
            sender.sendMessage(l10n.worlds.worldOperationSucceeded(operation, name))
        } catch (failure: IllegalArgumentException) {
            sender.sendMessage(l10n.worlds.worldOperationFailed(failure.message ?: "invalid world"))
        } catch (failure: IllegalStateException) {
            sender.sendMessage(l10n.worlds.worldOperationFailed(failure.message ?: "unsafe operation"))
        } catch (failure: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not $operation world $name", failure)
            sender.sendMessage(l10n.worlds.worldOperationFailed(failure.message ?: "check the server log for details"))
        }
    }

    private fun worldDefinition(
        name: String,
        environment: World.Environment,
        seed: Long,
    ) = WorldDefinition(
        registryKey = name,
        name = name,
        alias = "",
        environment = environment.name,
        worldType = WorldType.NORMAL.name,
        seed = seed,
        generator = null,
        generatorSettings = null,
        generateStructures = true,
        spawnLocation = null,
        scale = when (environment) {
            World.Environment.NETHER -> 8.0
            World.Environment.THE_END -> 16.0
            else -> 1.0
        },
        respawnWorld = null,
        pvp = true,
    )
}
