package com.projectcitybuild.pcbridge.paper.core.support.java

import java.time.Duration

fun Duration.humanReadable(): String {
    val totalSeconds = seconds

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (hours > 0 || minutes > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}