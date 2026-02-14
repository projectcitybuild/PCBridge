package com.projectcitybuild.pcbridge.http.shared.serialization.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.Instant

class InstantTypeAdapter : TypeAdapter<Instant>() {
    override fun write(out: JsonWriter, value: Instant) {
        out.value(value.toString()) // ISO-8601
    }

    override fun read(reader: JsonReader): Instant? {
        return if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            Instant.parse(reader.nextString())
        }
    }
}
