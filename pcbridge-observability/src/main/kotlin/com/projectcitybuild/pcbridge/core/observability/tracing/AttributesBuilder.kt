package com.projectcitybuild.pcbridge.core.observability.tracing

typealias Attributes = Map<String, Any?>

class AttributesBuilder {
    private val attributes = mutableMapOf<String, Any?>()

    fun put(
        key: String,
        value: Any?,
    ): AttributesBuilder {
        attributes[key] = value
        return this
    }

    fun build(): Attributes = attributes.toMap()
}