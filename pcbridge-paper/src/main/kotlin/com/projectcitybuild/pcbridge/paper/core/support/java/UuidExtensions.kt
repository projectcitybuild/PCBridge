package com.projectcitybuild.pcbridge.paper.core.support.java

import java.util.UUID

/**
 * Same as [UUID.fromString] but allows a UUID
 * string that doesn't have dashes
 */
fun uuidFromUnsanitizedString(input: String): UUID {
    // Length is 36 with dashes
    if (input.length != 32) {
        return UUID.fromString(input)
    }
    val buffer = StringBuilder(input).apply {
        insert(20, '-')
        insert(16, '-')
        insert(12, '-')
        insert(8, '-');
    }
    return UUID.fromString(buffer.toString());
}