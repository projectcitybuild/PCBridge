package com.projectcitybuild.modules.datetime.formatter

import java.time.LocalDateTime
import java.time.format.FormatStyle

interface DateTimeFormatter {
    fun convert(
        timestampInSeconds: Long,
        formatStyle: FormatStyle = FormatStyle.MEDIUM
    ): String

    fun convert(
        localDateTime: LocalDateTime,
        formatStyle: FormatStyle = FormatStyle.MEDIUM
    ): String
}