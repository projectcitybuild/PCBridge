package com.projectcitybuild.modules.textcomponentbuilder

import net.md_5.bungee.api.chat.BaseComponent

interface ChatMessageReceiver {
    fun sendMessage(component: BaseComponent)
}