package com.projectcitybuild.modules.datetime

import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatterImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.Locale

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
}