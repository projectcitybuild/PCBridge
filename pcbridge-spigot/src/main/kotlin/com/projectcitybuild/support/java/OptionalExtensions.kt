package com.projectcitybuild.support.java

import java.util.Optional

/**
 * Casts an Optional<T> to T?
 */
fun <T> Optional<T>.orNull(): T? = orElse(null)