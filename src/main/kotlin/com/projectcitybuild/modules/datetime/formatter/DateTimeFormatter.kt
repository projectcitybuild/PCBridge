package com.projectcitybuild.modules.datetime.formatter

import java.time.format.FormatStyle

interface DateTimeFormatter {
    fun convert(timestampInSeconds: Long, formatStyle: FormatStyle = FormatStyle.MEDIUM): String
}
