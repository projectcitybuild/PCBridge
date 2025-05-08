package com.projectcitybuild.pcbridge.paper.features.borders.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class BorderTypeAdapter: JsonSerializer<Border>, JsonDeserializer<Border> {
    override fun serialize(
        src: Border,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        return context.serialize(src).asJsonObject.apply {
            addProperty("type", when (src) {
                is Border.Rectangle -> "rectangle"
                is Border.Circle -> "circle"
            })
        }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Border {
        val jsonObj = json.asJsonObject

        return when (val type = jsonObj.get("type").asString) {
            "rectangle" -> context.deserialize(jsonObj, Border.Rectangle::class.java)
            "circle" -> context.deserialize(jsonObj, Border.Circle::class.java)
            else -> throw JsonParseException("Unknown type: $type")
        }
    }
}