package com.projectcitybuild.pcbridge.features.inventories.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.inventories.InventoriesPermissions
import com.projectcitybuild.pcbridge.features.inventories.domain.repositories.MultiverseInventoryRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import org.bukkit.plugin.Plugin

class InventoriesCommand(
    private val plugin: Plugin,
    private val repository: MultiverseInventoryRepository,
) : BrigadierCommand {
    override val builder = command("inventories") {
        description = l10n.inventories.commandDescription
        requiresPermission(InventoriesPermissions.MANAGE)

        literal("inspect") {
            executesSuspending(plugin) { context ->
                val report = repository.inspect()
                context.source.sender.sendMessage(
                    l10n.inventories.compatibilityReport(
                        worlds = report.worldProfiles,
                        groups = report.groupProfiles,
                        global = report.globalProfiles,
                        malformed = report.malformedJsonFiles.size + report.malformedYamlFiles.size,
                    ),
                )
            }
        }

        literal("groups") {
            executesSuspending(plugin) { context ->
                val groups =
                    repository.readWorldGroups().joinToString(", ") { group ->
                        "${group.name}[${group.worlds.joinToString("/")}; ${group.shares.joinToString("/")}]"
                    }.ifEmpty { "none" }
                context.source.sender.sendMessage(l10n.inventories.inventoryGroups(groups))
            }
        }

        literal("conflicts") {
            executesSuspending(plugin) { context ->
                val conflicts =
                    repository.findGroupShareConflicts().joinToString(", ") {
                        "${it.world}:${it.share} (${it.groups.joinToString("/")})"
                    }.ifEmpty { "none" }
                context.source.sender.sendMessage(l10n.inventories.inventoryConflicts(conflicts))
            }
        }
    }
}
