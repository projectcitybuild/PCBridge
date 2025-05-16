package com.projectcitybuild.pcbridge.paper.features.chatemojis

import com.projectcitybuild.pcbridge.paper.features.chatemojis.decorators.ChatEmojiDecorator
import org.koin.dsl.module

val chatEmojisModule = module {
    factory {
        ChatEmojiDecorator()
    }
}