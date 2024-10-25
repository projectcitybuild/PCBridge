@file:Suppress("ktlint:standard:max-line-length")

package com.projectcitybuild.pcbridge.http.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Key-values for the plugin fetched from the PCB web backend
 */
@Serializable
data class RemoteConfigKeyValues(
    val localization: Localization = Localization(),
    val chat: Chat = Chat(),
    val warps: Warps = Warps(),
    val integrations: Integrations = Integrations(),
    val announcements: Announcements = Announcements(),
    val messages: Messages = Messages(),
) {
    @Serializable
    data class Localization(
        @SerializedName("time_zone")
        val timeZone: String = "UTC",
        val locale: String = "en-us",
    )

    @Serializable
    data class Chat(
        @SerializedName("badge_icon")
        val badgeIcon: String = "<color:yellow>★</color>",
    )

    @Serializable
    data class Warps(
        @SerializedName("items_per_page")
        val itemsPerPage: Int = 15,
    )

    @Serializable
    data class Integrations(
        @SerializedName("dynmap_warp_icon_name")
        val dynmapWarpIconName: String = "portal",
    )

    @Serializable
    data class Announcements(
        @SerializedName("interval_in_mins")
        val intervalInMins: Int = 30,
        val messages: List<String> = emptyList(),
    )

    @Serializable
    data class Messages(
        val join: String = "<color:green><b>+</b></color> <name> <color:gray>joined the server</color>",
        val leave: String = "<color:red><b>-</b></color> <name> <color:gray>left the server (online for <time_online>)</color>",
        @SerializedName("first_time_join")
        val firstTimeJoin: String = "<color:AA00AA><b>✦ Welcome §f%name%§d to the server!</b></color>",
        val welcome: String = "<b>Welcome to PCB!</b>",
    )
}

@Serializable
data class RemoteConfigVersion(
    val version: Int,
    val config: RemoteConfigKeyValues,
)