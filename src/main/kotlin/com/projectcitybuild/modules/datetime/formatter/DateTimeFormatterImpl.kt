package com.projectcitybuild.modules.datetime.formatter

import dagger.Reusable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DateTimeFormatterImpl @Inject constructor(
    private val locale: Locale,
    private val timezone: ZoneId,
): DateTimeFormatter {

    private fun makeFormatter(formatStyle: FormatStyle): java.time.format.DateTimeFormatter {
        return java.time.format.DateTimeFormatter
            .ofLocalizedDateTime(formatStyle)
            .withLocale(locale)
    }

    override fun convert(timestampInSeconds: Long, formatStyle: FormatStyle): String {
        val secondsSince1970 = TimeUnit.SECONDS.toSeconds(timestampInSeconds)
        val dateTime = Instant.ofEpochSecond(secondsSince1970).atZone(timezone)
        val formatter = makeFormatter(formatStyle)

        return dateTime.format(formatter)
    }

    override fun convert(localDateTime: LocalDateTime, formatStyle: FormatStyle): String {
        val formatter = makeFormatter(formatStyle)

        return localDateTime.format(formatter)
    }
}