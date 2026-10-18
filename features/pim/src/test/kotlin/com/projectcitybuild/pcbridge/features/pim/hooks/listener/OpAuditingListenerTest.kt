package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.features.pim.domain.repositories.OpAuditRepository
import kotlinx.coroutines.test.runTest
import org.bukkit.command.CommandSender
import org.bukkit.event.server.ServerCommandEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class OpAuditingListenerTest {
    private val repository = mock(OpAuditRepository::class.java)
    private val listener = OpAuditingListener(repository)

    @Test
    fun `audits normalized op command with its sender`() =
        runTest {
            val sender = mock(CommandSender::class.java)
            val event = mock(ServerCommandEvent::class.java)
            whenever(event.command).thenReturn("/OP PlayerOne")
            whenever(event.sender).thenReturn(sender)

            listener.onServerCommandEvent(event)

            verify(repository).auditCommand("op playerone", OpAuditRepository.Actor.Unknown)
        }

    @Test
    fun `audits deop commands and ignores unrelated or incomplete commands`() =
        runTest {
            val sender = mock(CommandSender::class.java)
            val event = mock(ServerCommandEvent::class.java)
            whenever(event.command).thenReturn("deop PlayerOne")
            whenever(event.sender).thenReturn(sender)

            listener.onServerCommandEvent(event)
            verify(repository).auditCommand("deop playerone", OpAuditRepository.Actor.Unknown)

            whenever(event.command).thenReturn("op")
            listener.onServerCommandEvent(event)
            whenever(event.command).thenReturn("say hello")
            listener.onServerCommandEvent(event)

            verify(repository, never()).auditCommand("op", OpAuditRepository.Actor.Unknown)
            verify(repository, never()).auditCommand("say hello", OpAuditRepository.Actor.Unknown)
        }
}
