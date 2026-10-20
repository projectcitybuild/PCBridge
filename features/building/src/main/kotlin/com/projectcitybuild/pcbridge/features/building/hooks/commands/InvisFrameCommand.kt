package com.projectcitybuild.pcbridge.features.building.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.building.BuildingPermissions
import com.projectcitybuild.pcbridge.features.building.buildingTracer
import com.projectcitybuild.pcbridge.features.building.domain.data.InvisFrameKey
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.namespace.SpigotNamespace
import com.projectcitybuild.pcbridge.runtime.commands.scopedSync
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class InvisFrameCommand(
    private val plugin: Plugin,
    private val spigotNamespace: SpigotNamespace,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    internal var frameFactory: (Material) -> ItemStack = ::ItemStack

    override val description: String = "Gives you an itemframe that turns invisible when holding an item"

    override fun literal() = command("invisframe") {
        requiresPermission(BuildingPermissions.INVIS_FRAME)
        literal("glowing") {
            executesSuspending(commandTaskExecutor, ::giveGlowing)
        }
        executesSuspending(commandTaskExecutor, ::giveNormal)
    }

    private fun giveNormal(context: PaperCommandContext) = give(
        context = context,
        glowing = false,
    )

    private fun giveGlowing(context: PaperCommandContext) = give(
        context = context,
        glowing = true,
    )

    private fun give(
        context: PaperCommandContext,
        glowing: Boolean,
    ) = context.scopedSync(buildingTracer) {
        val player = context.source.requirePlayer()
        val itemStack =
            if (glowing) {
                frameFactory(Material.GLOW_ITEM_FRAME)
            } else {
                frameFactory(Material.ITEM_FRAME)
            }
        val itemName =
            if (glowing) {
                "Invisible Glowing Frame"
            } else {
                "Invisible Item Frame"
            }
        player.inventory.addItem(
            itemStack.apply {
                editMeta { meta ->
                    meta.displayName(
                        Component
                            .text(itemName)
                            .color(NamedTextColor.LIGHT_PURPLE)
                            .decorate(TextDecoration.ITALIC),
                    )
                }
                editPersistentDataContainer { pdc ->
                    pdc.set(
                        spigotNamespace.get(InvisFrameKey),
                        PersistentDataType.BOOLEAN,
                        true,
                    )
                }
            },
        )
        player.sendMessage(
            if (glowing) {
                l10n.building.receivedInvisFrameGlowing
            } else {
                l10n.building.receivedInvisFrame
            },
        )
    }
}
