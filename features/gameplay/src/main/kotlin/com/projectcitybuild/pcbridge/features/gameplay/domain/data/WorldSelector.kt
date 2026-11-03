package com.projectcitybuild.pcbridge.features.gameplay.domain.data

sealed interface WorldSelector {
    data object Current : WorldSelector

    data object All : WorldSelector

    data class Named(
        val name: String,
    ) : WorldSelector
}
