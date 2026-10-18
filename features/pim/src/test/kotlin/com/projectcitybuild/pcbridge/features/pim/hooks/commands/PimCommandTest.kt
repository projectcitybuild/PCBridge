package com.projectcitybuild.pcbridge.features.pim.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.features.pim.PimPermissions
import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.permissions.Permissions
import io.papermc.paper.command.brigadier.CommandSourceStack
import java.util.UUID
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class PimCommandTest {
    @Test
    fun `registers the op and roles subcommands behind the pim permission`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val plugin = mock(Plugin::class.java)
            val elevationService = mock(OpElevationService::class.java)
            val command =
                PimCommand(
                    OpGrantCommand(plugin, elevationService, mock(LocalizedTime::class.java), executor),
                    OpRevokeCommand(plugin, elevationService, executor),
                    OpStatusCommand(plugin, elevationService, mock(LocalizedTime::class.java), executor),
                    RolesDebugCommand(plugin, Permissions(), executor),
                )
            val node = command.literal()
            val source = mock(CommandSourceStack::class.java)
            val player = mock(Player::class.java)
            whenever(source.sender).thenReturn(player)
            whenever(player.uniqueId).thenReturn(UUID.randomUUID())
            whenever(player.hasPermission(PimPermissions.OP.node)).thenReturn(true)

            assertEquals(setOf("op", "roles"), node.children.map { it.name }.toSet())
            assertTrue(node.requirement.test(source))

            val dispatcher = CommandDispatcher<CommandSourceStack>()
            dispatcher.root.addChild(node)
            dispatcher.execute("pim op revoke", source)
            runCurrent()
            verify(elevationService).revoke(org.mockito.kotlin.any())

            whenever(player.hasPermission(PimPermissions.OP.node)).thenReturn(false)
            assertFalse(node.requirement.test(source))
        }
}
