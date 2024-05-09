package com.projectcitybuild.core.datetime

import org.jetbrains.annotations.TestOnly
import java.time.Instant
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit

class DateTimeFormatter(
    private val locale: Locale,
    private val timezone: ZoneId,
) {
    fun convert(
        timestampInSeconds: Long,
        formatStyle: FormatStyle = FormatStyle.MEDIUM,
    ): String {
        val formatter = java.time.format.DateTimeFormatter
            .ofLocalizedDateTime(formatStyle)
            .withLocale(locale)

        val secondsSince1970 = TimeUnit.SECONDS.toSeconds(timestampInSeconds)
        val dateTime = Instant.ofEpochSecond(secondsSince1970).atZone(timezone)

        return dateTime.format(formatter)
    }

    // TODO: move this
    companion object {
        @TestOnly
        fun mock() = DateTimeFormatter(
            Locale.forLanguageTag("en-us"),
            ZoneId.of("UTC"),
        )
    }
}
