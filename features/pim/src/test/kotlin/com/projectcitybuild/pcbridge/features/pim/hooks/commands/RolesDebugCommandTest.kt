package com.projectcitybuild.pcbridge.features.pim.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.features.pim.PimPermissions
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.permissions.Permissions
import com.projectcitybuild.pcbridge.runtime.permissions.PermissionsProvider
import io.papermc.paper.command.brigadier.CommandSourceStack
import java.util.UUID
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class RolesDebugCommandTest {
    @Test
    fun `sets the supplied roles and reports how to revert them`() =
        runTest {
            val player = mock(Player::class.java)
            val source = mock(CommandSourceStack::class.java)
            val provider = mock(PermissionsProvider::class.java)
            val permissions = Permissions().apply { setProvider(provider) }
            val playerId = UUID.randomUUID()
            whenever(source.sender).thenReturn(player)
            whenever(player.uniqueId).thenReturn(playerId)
            whenever(player.hasPermission(PimPermissions.ROLES.node)).thenReturn(true)
            val command =
                RolesDebugCommand(
                    mock(Plugin::class.java),
                    permissions,
                    CoroutineScopeCommandTaskExecutor(this),
                )
            val dispatcher = CommandDispatcher<CommandSourceStack>()
            dispatcher.root.addChild(command.literal())

            dispatcher.execute("debug builder staff", source)
            runCurrent()

            verify(provider).setUserRoles(playerId, setOf("builder", "staff"))
            verify(player).sendRichMessage(
                "<red>Your roles have been set to builder,staff</red>\n<gray>Use /sync or reconnect to revert this</gray>",
            )
        }
}
