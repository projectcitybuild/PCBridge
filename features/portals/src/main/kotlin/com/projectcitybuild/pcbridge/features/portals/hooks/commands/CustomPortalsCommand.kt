package com.projectcitybuild.pcbridge.features.portals.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.portals.PortalsPermissions
import com.projectcitybuild.pcbridge.features.portals.domain.services.CustomPortalService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class CustomPortalsCommand(
    private val plugin: Plugin,
    private val service: CustomPortalService,
) : BrigadierCommand {
    private val portalName = Argument.string("name")
    private val destination = Argument.greedyString("destination")

    override val builder = command("mvp") {
        description = l10n.portals.customCommandDescription
        requiresPermission(PortalsPermissions.MANAGE)

        literal("wand") {
            executesSuspending(plugin) { context ->
                val player = context.requirePlayer() ?: return@executesSuspending
                service.enableWand(player)
            }
        }

        literal("list") {
            executesSuspending(plugin) { context ->
                val portals = service.all().joinToString(", ") { it.name }.ifEmpty { "none" }
                context.source.sender.sendMessage(l10n.portals.customList(portals))
            }
        }

        literal("create") {
            argument(portalName) {
                executesSuspending(plugin) { context -> create(context, "") }
                argument(destination) {
                    executesSuspending(plugin) { context ->
                        create(context, destination.get(context))
                    }
                }
            }
        }

        literal("destination") {
            argument(portalName) {
                argument(destination) {
                    executesSuspending(plugin) { context ->
                        val name = portalName.get(context)
                        try {
                            service.setDestination(name, destination.get(context))
                            context.source.sender.sendMessage(l10n.portals.customDestinationSet(name))
                        } catch (failure: IllegalArgumentException) {
                            context.source.sender.sendMessage(l10n.errors.commandError(failure.message))
                        } catch (failure: IllegalStateException) {
                            context.source.sender.sendMessage(l10n.errors.commandError(failure.message))
                        }
                    }
                }
            }
        }

        literal("remove") {
            argument(portalName) {
                executesSuspending(plugin) { context ->
                    val name = portalName.get(context)
                    try {
                        if (service.remove(name)) {
                            context.source.sender.sendMessage(l10n.portals.customRemoved(name))
                        } else {
                            context.source.sender.sendMessage(l10n.portals.customNotFound(name))
                        }
                    } catch (failure: IllegalArgumentException) {
                        context.source.sender.sendMessage(l10n.errors.commandError(failure.message))
                    } catch (failure: IllegalStateException) {
                        context.source.sender.sendMessage(l10n.errors.commandError(failure.message))
                    }
                }
            }
        }

        literal("reload") {
            executesSuspending(plugin) { context ->
                service.reload()
                context.source.sender.sendMessage(l10n.portals.customReloaded)
            }
        }
    }

    private fun create(
        context: PaperCommandContext,
        destination: String,
    ) {
        val player = context.requirePlayer() ?: return
        val name = portalName.get(context)
        try {
            service.create(player, name, destination)
            context.source.sender.sendMessage(l10n.portals.customCreated(name))
        } catch (failure: IllegalArgumentException) {
            context.source.sender.sendMessage(l10n.errors.commandError(failure.message))
        } catch (failure: IllegalStateException) {
            context.source.sender.sendMessage(l10n.errors.commandError(failure.message))
        }
    }

    private fun PaperCommandContext.requirePlayer(): Player? {
        val player = source.sender as? Player
        if (player == null) source.sender.sendMessage(l10n.portals.customPlayerOnly)
        return player
    }
}
