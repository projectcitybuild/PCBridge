package com.projectcitybuild.pcbridge.features.worlds.domain.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class SerializableWorldBorderTypeAdapter : TypeAdapter<SerializableWorldBorder>() {
    override fun write(
        out: JsonWriter,
        value: SerializableWorldBorder?,
    ) {
        if (value == null) {
            out.nullValue()
            return
        }

        out.beginObject()
        out.name("shape").value(
            when (value) {
                is SerializableWorldBorder.Square -> "SQUARE"
                is SerializableWorldBorder.Circle -> "CIRCLE"
                is SerializableWorldBorder.Rectangle -> "RECTANGLE"
            },
        )
        out.name("center_x").value(value.centerX)
        out.name("center_z").value(value.centerZ)
        when (value) {
            is SerializableWorldBorder.Square -> out.name("radius").value(value.radius)
            is SerializableWorldBorder.Circle -> out.name("radius").value(value.radius)
            is SerializableWorldBorder.Rectangle -> {
                out.name("width").value(value.width)
                out.name("depth").value(value.depth)
            }
        }
        out.endObject()
    }

    override fun read(input: JsonReader): SerializableWorldBorder? {
        if (input.peek() == JsonToken.NULL) {
            input.nextNull()
            return null
        }

        val json = JsonParser.parseReader(input)
        if (!json.isJsonObject) throw JsonParseException("World border must be a JSON object")
        val border = json.asJsonObject
        val centerX = border.requiredDouble("center_x")
        val centerZ = border.requiredDouble("center_z")

        return when (val shape = border.requiredString("shape")) {
            "SQUARE" -> SerializableWorldBorder.Square(centerX, centerZ, border.requiredDouble("radius"))
            "CIRCLE" -> SerializableWorldBorder.Circle(centerX, centerZ, border.requiredDouble("radius"))
            "RECTANGLE" -> SerializableWorldBorder.Rectangle(
                centerX,
                centerZ,
                border.requiredDouble("width"),
                border.requiredDouble("depth"),
            )
            else -> throw JsonParseException("Unknown world border shape: $shape")
        }
    }

    private fun JsonObject.requiredDouble(name: String): Double {
        val value = requiredElement(name)
        if (!value.isJsonPrimitive || !value.asJsonPrimitive.isNumber) {
            throw JsonParseException("Expected numeric value for '$name'")
        }
        return value.asDouble
    }

    private fun JsonObject.requiredString(name: String): String {
        val value = requiredElement(name)
        if (!value.isJsonPrimitive || !value.asJsonPrimitive.isString) {
            throw JsonParseException("Expected string value for '$name'")
        }
        return value.asString
    }

    private fun JsonObject.requiredElement(name: String): JsonElement =
        get(name)?.takeUnless(JsonElement::isJsonNull)
            ?: throw JsonParseException("Missing required field '$name'")
}
