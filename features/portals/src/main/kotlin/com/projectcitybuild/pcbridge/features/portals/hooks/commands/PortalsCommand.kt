package com.projectcitybuild.pcbridge.features.portals.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.portals.PortalsPermissions
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalLinkType
import com.projectcitybuild.pcbridge.features.portals.domain.repositories.PortalLinksRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import org.bukkit.plugin.Plugin

class PortalsCommand(
    private val plugin: Plugin,
    private val portalLinksRepository: PortalLinksRepository,
) : BrigadierCommand {
    private val sourceWorld = Argument.string("source")
    private val destinationWorld = Argument.string("destination")
    private val linkType = Argument.enumValue("type", PortalLinkType::class.java) { it.configKey }
    private val bidirectional = Argument.onOff("bidirectional")

    override val builder = command("portals") {
        description = l10n.portals.commandDescription
        requiresPermission(PortalsPermissions.MANAGE)

        literal("list") {
            executesSuspending(plugin) { context ->
                val links = portalLinksRepository
                    .all()
                    .joinToString(", ") { "${it.sourceWorld}:${it.type.configKey}->${it.destinationWorld}" }
                    .ifEmpty { "none" }
                context.source.sender.sendMessage(l10n.portals.links(links))
            }
        }

        literal("link") {
            argument(sourceWorld) {
                argument(linkType) {
                    argument(destinationWorld) {
                        executesSuspending(plugin) { context ->
                            createLink(context, bidirectional = false)
                        }
                        literal("bidirectional") {
                            argument(bidirectional) {
                                executesSuspending(plugin) { context ->
                                    createLink(context, bidirectional.get(context))
                                }
                            }
                        }
                    }
                }
            }
        }

        literal("unlink") {
            argument(sourceWorld) {
                argument(linkType) {
                    executesSuspending(plugin) { context ->
                        removeLink(context, bidirectional = false)
                    }
                    literal("bidirectional") {
                        argument(bidirectional) {
                            executesSuspending(plugin) { context ->
                                removeLink(context, bidirectional.get(context))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createLink(
        context: PaperCommandContext,
        bidirectional: Boolean,
    ) {
        val source = sourceWorld.get(context)
        val destination = destinationWorld.get(context)
        val type = linkType.get(context)
        try {
            portalLinksRepository.link(source, destination, type, bidirectional)
            context.source.sender.sendMessage(l10n.portals.linkCreated(source, type.configKey, destination, bidirectional))
        } catch (failure: IllegalArgumentException) {
            context.source.sender.sendMessage(l10n.errors.commandError(failure.message))
        }
    }

    private fun removeLink(
        context: PaperCommandContext,
        bidirectional: Boolean,
    ) {
        val source = sourceWorld.get(context)
        val type = linkType.get(context)
        try {
            if (portalLinksRepository.unlink(source, type, bidirectional)) {
                context.source.sender.sendMessage(l10n.portals.linkRemoved(source, type.configKey))
            } else {
                context.source.sender.sendMessage(l10n.portals.noLink(source, type.configKey))
            }
        } catch (failure: IllegalArgumentException) {
            context.source.sender.sendMessage(l10n.errors.commandError(failure.message))
        }
    }
}
