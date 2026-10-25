package com.projectcitybuild.pcbridge.app.paperplugin

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PluginCleanupTest {
    @Test
    fun `container and owned global Koin close even when application stop fails`() =
        runTest {
            val application = mock<PCBridgeApplication>()
            val failure = IllegalStateException("application shutdown failed")
            doThrow(failure).whenever(application).stop()
            val calls = mutableListOf<String>()

            val thrown = try {
                shutdownPluginRuntime(
                    application = application,
                    closeContainer = { calls.add("container") },
                    ownsGlobalKoin = true,
                    stopGlobalKoin = { calls.add("global") },
                )
                null
            } catch (caught: Throwable) {
                caught
            }

            assertSame(failure, thrown)
            assertEquals(listOf("container", "global"), calls)
        }
}
