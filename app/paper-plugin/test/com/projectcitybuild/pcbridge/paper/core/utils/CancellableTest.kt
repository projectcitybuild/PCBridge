package com.projectcitybuild.pcbridge.paper.core.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CancellableTest {
    @Test
    fun `should run cancellable handler`() {
        var didCancel = false
        val cancellable = Cancellable { didCancel = true }

        assertFalse(didCancel)
        cancellable.cancel()

        assertTrue(didCancel)
    }

    @Test
    fun `getter should return cancelled state`() {
        val cancellable = Cancellable {}

        assertFalse(cancellable.isCancelled)
        cancellable.cancel()
        assertTrue(cancellable.isCancelled)
    }
}
