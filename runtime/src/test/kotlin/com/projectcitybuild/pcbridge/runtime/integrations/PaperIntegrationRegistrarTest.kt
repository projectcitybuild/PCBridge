package com.projectcitybuild.pcbridge.runtime.integrations

import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegration
import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegrationRegistrar
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class PaperIntegrationRegistrarTest {
    @Test
    fun `integrations are enabled and disabled in priority order`() =
        runTest {
            val calls = mutableListOf<String>()
            val registrar = PaperIntegrationRegistrar()
            val integrations =
                listOf(
                    integration("third", 300, calls),
                    integration("first", 100, calls),
                    integration("second", 200, calls),
                )

            registrar.enable(integrations)
            registrar.disable()

            assertEquals(
                listOf(
                    "enable:first",
                    "enable:second",
                    "enable:third",
                    "disable:third",
                    "disable:second",
                    "disable:first",
                ),
                calls,
            )
        }

    @Test
    fun `failed enablement disables only successful integrations in reverse order`() =
        runTest {
            val calls = mutableListOf<String>()
            val registrar = PaperIntegrationRegistrar()
            val failure = IllegalStateException("enable failed")
            val integrations = listOf(
                integration("first", 100, calls),
                integration("second", 200, calls, enableAction = {
                    throw failure
                }),
                integration("third", 300, calls),
            )

            val thrown = try {
                registrar.enable(integrations)
                null
            } catch (caught: Throwable) {
                caught
            }

            assertSame(failure, thrown)
            assertEquals(
                listOf("enable:first", "enable:second", "disable:first"),
                calls,
            )
        }

    @Test
    fun `disable continues after an integration cleanup fails`() =
        runTest {
            val calls = mutableListOf<String>()
            val registrar = PaperIntegrationRegistrar()
            val failure = IllegalStateException("disable failed")
            registrar.enable(
                listOf(
                    integration("first", 100, calls),
                    integration("second", 200, calls, disableAction = { throw failure }),
                ),
            )

            val thrown = try {
                registrar.disable()
                null
            } catch (caught: Throwable) {
                caught
            }

            assertSame(failure, thrown)
            assertEquals(
                listOf(
                    "enable:first",
                    "enable:second",
                    "disable:second",
                    "disable:first",
                ),
                calls,
            )
        }

    private fun integration(
        name: String,
        priority: Int,
        calls: MutableList<String>,
        enableAction: suspend () -> Unit = {},
        disableAction: () -> Unit = {},
    ) = object : PaperIntegration {
        override val priority = priority

        override suspend fun enable() {
            calls.add("enable:$name")
            enableAction()
        }

        override fun disable() {
            calls.add("disable:$name")
            disableAction()
        }
    }
}
