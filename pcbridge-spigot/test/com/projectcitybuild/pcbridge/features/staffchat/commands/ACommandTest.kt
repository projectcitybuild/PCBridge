package com.projectcitybuild.pcbridge.features.staffchat.commands

import com.projectcitybuild.pcbridge.Permissions
import com.projectcitybuild.pcbridge.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.http.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.http.models.RemoteConfigVersion
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ACommandTest {
    private lateinit var server: Server
    private lateinit var remoteConfig: RemoteConfig

    @BeforeEach
    fun setUp() {
        server = mock(Server::class.java)
        remoteConfig = mock(RemoteConfig::class.java)
    }

    @Test
    fun `only players with permission receive staff messages`() =
        runTest {
            val regularPlayer =
                mock(Player::class.java).also {
                    whenever(it.hasPermission(Permissions.COMMAND_STAFF_CHAT)).thenReturn(false)
                }
            val staffPlayer =
                mock(Player::class.java).also {
                    whenever(it.hasPermission(Permissions.COMMAND_STAFF_CHAT)).thenReturn(true)
                }
            whenever(server.onlinePlayers).thenReturn(
                listOf(regularPlayer, staffPlayer),
            )
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    version = 1,
                    config = RemoteConfigKeyValues(
                        chat = RemoteConfigKeyValues.Chat(staffChannel = "staff: <name> <message>")
                    ),
                )
            )

            val sender = mock(Player::class.java)
            whenever(sender.name).thenReturn("user123")

            StaffChatCommand(server, remoteConfig).run(
                sender = sender,
                args =
                    StaffChatCommand.Args(
                        message = "foo bar",
                    ),
            )

            argumentCaptor<Component>().apply {
                verify(staffPlayer).sendMessage(capture())
                assertTrue(firstValue.toString().contains("staff: user123 foo bar"))
            }
            verify(regularPlayer, never()).sendMessage(any<Component>())
        }
}
