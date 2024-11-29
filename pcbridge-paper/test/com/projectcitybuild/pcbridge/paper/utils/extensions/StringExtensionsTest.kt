package com.projectcitybuild.pcbridge.paper.utils.extensions

import com.projectcitybuild.pcbridge.paper.core.extensions.toDashFormattedUUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringExtensionsTest {
    @Test
    fun `toDashFormattedUUID returns a UUID with dashes`() {
        val original = "f2437cde194a455c8a9b1fd602618609"
        val expected = "f2437cde-194a-455c-8a9b-1fd602618609"

        assertEquals(expected, original.toDashFormattedUUID())
    }
}
