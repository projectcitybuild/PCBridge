package com.projectcitybuild.pcbridge.paper.features.chatformatting

import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.decorators.ChatEmojiDecorator
import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.listeners.EmojiConfigListener
import com.projectcitybuild.pcbridge.paper.features.chatformatting.domain.repositories.EmojiRepository
import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.decorators.ChatUrlDecorator
import org.koin.dsl.module

val chatFormattingModule = module {
    factory {
        ChatEmojiDecorator(
            emojiRepository = get(),
        )
    }

    factory {
        ChatUrlDecorator()
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