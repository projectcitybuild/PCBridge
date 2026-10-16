package com.projectcitybuild.pcbridge.core.http.serialization

import com.google.gson.GsonBuilder
import com.projectcitybuild.pcbridge.core.http.serialization.gson.InstantTypeAdapter
import com.projectcitybuild.pcbridge.core.http.serialization.gson.LocalDateTimeTypeAdapter
import com.projectcitybuild.pcbridge.core.http.serialization.serializable.InstantSerializer
import com.projectcitybuild.pcbridge.core.http.serialization.serializable.LocalDateTimeSerializer
import com.projectcitybuild.pcbridge.core.http.serialization.serializable.UUIDSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

class DateTimeSerializersTest {
    @Test
    fun `gson adapters should round trip instant and local date time values`() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
            .create()
        val instant = Instant.parse("2026-10-16T12:30:45Z")
        val localDateTime = LocalDateTime.parse("2026-10-16T12:30:45")

        assertEquals(
            instant,
            gson.fromJson(gson.toJson(instant), Instant::class.java)
        )
        assertEquals(
            localDateTime,
            gson.fromJson(gson.toJson(localDateTime), LocalDateTime::class.java),
        )
    }

    @Test
    fun `kotlin serializers should use string representations for temporal and uuid values`() {
        val instant = Instant.parse("2026-10-16T12:30:45Z")
        val localDateTime = LocalDateTime.parse("2026-10-16T12:30:45")
        val uuid = UUID.fromString("39d2f76c-b1ca-4bfb-8720-b4bd40a6bc37")

        assertEquals(
            "\"$instant\"",
            Json.encodeToString(InstantSerializer, instant),
        )
        assertEquals(
            instant,
            Json.decodeFromString(InstantSerializer, Json.encodeToString(InstantSerializer, instant)),
        )
        assertEquals(
            localDateTime,
            Json.decodeFromString(
                LocalDateTimeSerializer,
                Json.encodeToString(LocalDateTimeSerializer, localDateTime),
            ),
        )
        assertEquals(
            uuid,
            Json.decodeFromString(UUIDSerializer, Json.encodeToString(UUIDSerializer, uuid)),
        )
    }
}
