package com.projectcitybuild

import com.projectcitybuild.libs.datetime.formatter.DateTimeFormatterImpl
import java.time.ZoneId
import java.util.Locale

fun DateTimeFormatterMock(): DateTimeFormatterImpl {
    return DateTimeFormatterImpl(
        Locale.forLanguageTag("en-us"),
        ZoneId.of("UTC"),
    )
}
