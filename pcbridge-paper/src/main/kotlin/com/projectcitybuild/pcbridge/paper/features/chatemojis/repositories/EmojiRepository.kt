package com.projectcitybuild.pcbridge.paper.features.chatemojis.repositories

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig

class EmojiRepository(
    remoteConfig: RemoteConfig,
) {
    private var mapping: Map<String, String> = mapOf()
    private var pattern: String = ""

    init {
        set(remoteConfig.latest.config.emojis)
    }

    val emojiPattern: String
        get() = pattern

    fun emoji(placeholder: String): String?
        = mapping[placeholder]

    fun set(emojis: Map<String, String>) {
        mapping = emojis
        pattern = emojis.keys
            .joinToString(separator = "|")
            .let { pattern -> "(?i)($pattern)" } // Add case-insensitivity
    }
}