package com.projectcitybuild.pcbridge.webserver.utils

import java.math.BigInteger
import java.util.UUID

/**
 * Same as UUID.fromString(), except that it allows a non-hyphen string
 */
internal fun uuidFromAnyString(string: String) = string.run {
    if (string.contains("-")) {
        UUID.fromString(string)
    } else {
        val bi1 = BigInteger(substring(0, 16), 16)
        val bi2 = BigInteger(substring(16, 32), 16)
        UUID(bi1.toLong(), bi2.toLong())
    }
}