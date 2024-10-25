package com.projectcitybuild.pcbridge.datetime

import com.projectcitybuild.pcbridge.core.datetime.DateTimeFormatter
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.Locale

class DateTimeFormatterTest {
    @Test
    fun `convert formats timestamp with correct locale and timezone`() {
        val formatter =
            DateTimeFormatter(
                Locale.forLanguageTag("en-us"),
                ZoneId.of("UTC"),
            )
        val result = formatter.convert(1642961224, FormatStyle.MEDIUM)

        // Java 20 introduces a Unicode space character (looks like <NNBSP>)
        // so we need to pattern match (\p{Zs} matches any Unicode space)
        //
        // See: https://github.com/spring-projects/spring-framework/wiki/Date-and-Time-Formatting-with-JDK-20-and-higher#options-for-application-code-and-tests
        assert(
            result.matches(Regex("Jan 23, 2022, 6:07:04\\p{Zs}PM"))
        )
    }
}
