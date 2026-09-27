package com.projectcitybuild.pcbridge.paper.runtime.integrations

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
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
            registrar.disable(integrations)

            assertEquals(
                listOf(
                    "enable:first",
                    "enable:second",
                    "enable:third",
                    "disable:first",
                    "disable:second",
                    "disable:third",
                ),
                calls,
            )
        }

    private fun integration(
        name: String,
        priority: Int,
        calls: MutableList<String>,
    ) = object : PaperIntegration {
        override val priority = priority

        override suspend fun enable() {
            calls.add("enable:$name")
        }

        override fun disable() {
            calls.add("disable:$name")
        }
    }
}
