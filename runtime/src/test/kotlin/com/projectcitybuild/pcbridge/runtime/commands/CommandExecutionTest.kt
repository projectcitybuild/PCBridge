package com.projectcitybuild.pcbridge.runtime.commands

import com.projectcitybuild.pcbridge.core.observability.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class CommandExecutionTest {
    @Test
    fun `safe command execution preserves expected user-facing error response`() =
        runBlocking {
            val sender = mock<CommandSender>()

            runCommandSafely(sender) {
                throw IllegalStateException("Expected command failure")
            }

            verify(sender).sendMessage(any<Component>())
        }

    @Test
    fun `safe command execution reports unexpected errors and sends the generic response`() =
        runBlocking {
            Logger.configure("pcbridge.command-execution-test")
            val sender = mock<CommandSender>()

            runCommandSafely(sender) {
                throw IllegalArgumentException("Unexpected command failure")
            }

            verify(sender).sendMessage(any<Component>())
        }

    @Test
    fun `safe command execution propagates cancellation without responding`() =
        runBlocking {
            val sender = mock<CommandSender>()
            val cancellation = CancellationException("cancelled")

            val thrown =
                assertThrows<CancellationException> {
                    runBlocking {
                        runCommandSafely(sender) { throw cancellation }
                    }
                }

            assertSame(cancellation, thrown)
            verify(sender, never()).sendMessage(any<Component>())
        }

    @Test
    fun `synchronous safe command execution preserves expected responses`() {
        val sender = mock<CommandSender>()

        runCommandSafelySync(sender) {
            throw IllegalStateException("Expected command failure")
        }

        verify(sender).sendMessage(any<Component>())
    }

    @Test
    fun `synchronous safe command execution propagates cancellation`() {
        val sender = mock<CommandSender>()
        val cancellation = CancellationException("cancelled")

        val thrown =
            assertThrows<CancellationException> {
                runCommandSafelySync(sender) { throw cancellation }
            }

        assertSame(cancellation, thrown)
        verify(sender, never()).sendMessage(any<Component>())
    }
}
