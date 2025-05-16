package com.projectcitybuild.pcbridge.paper.features.chaturls

import com.projectcitybuild.pcbridge.paper.features.chaturls.decorators.ChatUrlDecorator
import org.koin.dsl.module

val chatUrlsModule = module {
    factory {
        ChatUrlDecorator()
    }
}