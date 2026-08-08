package com.projectcitybuild.pcbridge.core.datetime.extensions

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun LocalDateTime.toISO8601(): String
    = OffsetDateTime.of(this, ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)
