package com.projectcitybuild.stubs

import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatterImpl
import java.time.ZoneId
import java.util.*

fun DateTimeFormatterMock(): DateTimeFormatterImpl {
    return DateTimeFormatterImpl(
        Locale.forLanguageTag("en-us"),
        ZoneId.of("UTC"),
    )
}
