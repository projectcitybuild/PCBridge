package com.projectcitybuild.support.textcomponent

import net.md_5.bungee.api.chat.BaseComponent

interface ChatMessageReceiver {
    fun sendMessage(component: BaseComponent)
}
