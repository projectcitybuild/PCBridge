package com.projectcitybuild.platforms.bungeecord.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun async(block: suspend (CoroutineScope) -> Unit) {

    // Calling Bungeecord APIs are supposedly thread-safe, so there isn't actually a concept
    // of "main" thread like Spigot (which schedules things on ticks).
    //
    // Bungeecord itself dispatches everything on Netty IO threads
    CoroutineScope(Dispatchers.IO).launch(block = block)
}