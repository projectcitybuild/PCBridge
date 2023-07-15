package com.projectcitybuild.pcbridge.core.modules.datetime.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class LocalizedTimeTest {

    @Test
    fun `returns current time based on given clock`() {
        val zoneId = ZoneOffset.UTC
        val fixedDate = LocalDate.of(2023, 7, 15).atStartOfDay().atZone(zoneId)

        val clock = Clock.fixed(
            fixedDate.toInstant(),
            zoneId,
        )
        val result = LocalizedTime(clock).now()

        assertEquals(fixedDate.toLocalDateTime(), result)
    }
}
