package com.projectcitybuild.pcbridge.core.modules.datetime.formatter

import java.time.format.FormatStyle

interface DateTimeFormatter {
    fun convert(
        timestampInSeconds: Long,
        formatStyle: FormatStyle = FormatStyle.MEDIUM,
    ): String
}
