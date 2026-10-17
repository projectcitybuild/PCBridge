package com.projectcitybuild.pcbridge.features.pim.hooks.commands

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.runtime.permissions.Permissions
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class PimCommandTest {
    @Test
    fun `registers the op and roles subcommands behind the pim permission`() {
        val command =
            PimCommand(
                OpGrantCommand(mock(Plugin::class.java), mock(OpElevationService::class.java), mock(LocalizedTime::class.java)),
                OpRevokeCommand(mock(Plugin::class.java), mock(OpElevationService::class.java)),
                OpStatusCommand(mock(Plugin::class.java), mock(OpElevationService::class.java), mock(LocalizedTime::class.java)),
                RolesDebugCommand(mock(Plugin::class.java), Permissions()),
            )
        val node = command.literal()
        val source = mock(CommandSourceStack::class.java)
        val sender = mock(CommandSender::class.java)
        whenever(source.sender).thenReturn(sender)
        whenever(sender.hasPermission(PermissionNode.PIM_OP.node)).thenReturn(true)

        assertEquals(setOf("op", "roles"), node.children.map { it.name }.toSet())
        assertTrue(node.requirement.test(source))
        whenever(sender.hasPermission(PermissionNode.PIM_OP.node)).thenReturn(false)
        assertFalse(node.requirement.test(source))
    }
}
