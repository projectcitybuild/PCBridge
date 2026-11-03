package com.projectcitybuild.pcbridge.runtime.commands

import com.projectcitybuild.pcbridge.core.l10n.LocalizedComponentException
import com.projectcitybuild.pcbridge.core.l10n.checkNotNullLocalized
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.observability.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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
    fun `safe command execution preserves localized component errors`() {
        runBlocking {
            val sender = mock<CommandSender>()
            val expectedText = l10n.warps.errorWarpNotFound("spawn")

            runCommandSafely(sender) {
                throw LocalizedComponentException(expectedText)
            }

            val response = argumentCaptor<Component>()
            verify(sender).sendMessage(response.capture())
            assertEquals(
                "Error: Could not find warp spawn",
                PlainTextComponentSerializer.plainText().serialize(response.firstValue),
            )
            assertTrue(
                response.firstValue.descendants().any {
                    it.style().color() == TextColor.color(0x64D8FF) &&
                        PlainTextComponentSerializer.plainText().serialize(it) == "spawn"
                },
            )
        }
    }

    @Test
    fun `localized null check smart casts its value`() {
        assertEquals(4, stringLengthAfterLocalizedCheck("warp"))
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

    private fun Component.descendants(): Sequence<Component> {
        return sequence {
            yield(this@descendants)
            children().forEach { yieldAll(it.descendants()) }
        }
    }

    private fun stringLengthAfterLocalizedCheck(value: String?): Int {
        checkNotNullLocalized(value) { Component.text("Missing value") }
        return value.length
    }
}
