package com.projectcitybuild.pcbridge.paper.features.chatformatting

import com.projectcitybuild.pcbridge.paper.features.chatformatting.domain.repositories.EmojiRepository
import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.decorators.ChatEmojiDecorator
import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.decorators.ChatUrlDecorator
import com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.listeners.EmojiConfigListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val chatFormattingModule =
    module {
        paperFeature("chat-formatting") {
            listeners(get<EmojiConfigListener>())
            chatMessageDecorator(get<ChatEmojiDecorator>(), priority = 100)
            chatMessageDecorator(get<ChatUrlDecorator>(), priority = 200)
        }

        factoryOf(::ChatEmojiDecorator)
        factoryOf(::ChatUrlDecorator)
        factoryOf(::EmojiConfigListener)
        singleOf(::EmojiRepository)
    }
