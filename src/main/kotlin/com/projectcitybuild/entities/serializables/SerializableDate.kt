package com.projectcitybuild.entities.serializables

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class SerializableDate(
    @Serializable(with = DateSerializer::class)
    val unwrapped: Date
)

object DateSerializer: KSerializer<Date> {
    private val df: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")

    override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        return df.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(df.format(value))
    }
}