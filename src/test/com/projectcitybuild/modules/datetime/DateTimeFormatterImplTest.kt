package com.projectcitybuild.modules.datetime

import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatterImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.*

class DateTimeFormatterImplTest {

    @Test
    fun `convert formats timestamp with correct locale and timezone`() {
        val formatter = DateTimeFormatterImpl(
            Locale.forLanguageTag("en-us"),
            ZoneId.of("UTC"),
        )
        val result = formatter.convert(1642961224, FormatStyle.MEDIUM)

        assertEquals("Jan 23, 2022, 6:07:04 PM", result)
    }

    @Test
    fun `convert formats LocalDateTime with correct locale and timezone`() {
        val formatter = DateTimeFormatterImpl(
            Locale.forLanguageTag("en-us"),
            ZoneId.of("UTC"),
        )
        val localDateTime = LocalDateTime.of(2022, Month.JANUARY, 23, 18, 7, 4)
        val result = formatter.convert(localDateTime, FormatStyle.MEDIUM)

        assertEquals("Jan 23, 2022, 6:07:04 PM", result)
    }
}