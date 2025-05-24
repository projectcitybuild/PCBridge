package com.projectcitybuild.pcbridge.paper.features.chatemojis

import com.projectcitybuild.pcbridge.paper.features.chatemojis.decorators.ChatEmojiDecorator
import com.projectcitybuild.pcbridge.paper.features.chatemojis.listeners.EmojiConfigListener
import com.projectcitybuild.pcbridge.paper.features.chatemojis.repositories.EmojiRepository
import org.koin.dsl.module

val chatEmojisModule = module {
    factory {
        ChatEmojiDecorator(
            emojiRepository = get(),
        )
    }

    factory {
        EmojiConfigListener(
            emojiRepository = get(),
        )
    }

    single {
        EmojiRepository(
            remoteConfig = get(),
        )
    }
}