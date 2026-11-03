package com.projectcitybuild.pcbridge.core.l10n

import net.kyori.adventure.text.Component
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class LocalizedComponentException(
    val component: Component,
) : RuntimeException()

@OptIn(ExperimentalContracts::class)
inline fun <T : Any> checkNotNullLocalized(
    value: T?,
    message: () -> Component,
): T {
    contract {
        returns() implies (value != null)
    }

    return value ?: throw LocalizedComponentException(message())
}

@OptIn(ExperimentalContracts::class)
inline fun checkLocalized(
    condition: Boolean,
    message: () -> Component,
) {
    contract {
        returns() implies condition
    }

    if (!condition) {
        throw LocalizedComponentException(message())
    }
}
