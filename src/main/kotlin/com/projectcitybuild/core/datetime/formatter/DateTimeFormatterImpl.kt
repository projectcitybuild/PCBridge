package com.projectcitybuild.core.datetime.formatter

import dagger.Reusable
import java.time.Instant
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DateTimeFormatterImpl @Inject constructor(
    private val locale: Locale,
    private val timezone: ZoneId,
) : DateTimeFormatter {

    override fun convert(timestampInSeconds: Long, formatStyle: FormatStyle): String {
        val formatter = java.time.format.DateTimeFormatter
            .ofLocalizedDateTime(formatStyle)
            .withLocale(locale)

        val secondsSince1970 = TimeUnit.SECONDS.toSeconds(timestampInSeconds)
        val dateTime = Instant.ofEpochSecond(secondsSince1970).atZone(timezone)

        return dateTime.format(formatter)
    }
}
