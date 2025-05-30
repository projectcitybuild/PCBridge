@file:Suppress("ktlint:standard:max-line-length")

package com.projectcitybuild.pcbridge.http.pcb.models

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
    val motd: String = "",
    val tab: Tab = Tab(),
    @SerializedName("server_links")
    val serverLinks: ServerLinks = ServerLinks(),
    val hub: Hub? = null,
    val emojis: Map<String, String> = mapOf(),
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
        @SerializedName("staff_channel")
        val staffChannel: String = "<yellow>(Staff) <name>:</yellow> <message>"
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
        @SerializedName("dynmap_spawn_icon_name")
        val dynmapSpawnIconName: String = "world",
    )

    @Serializable
    data class Announcements(
        @SerializedName("interval_in_mins")
        val intervalInMins: Int = 30,
        val messages: List<String> = emptyList(),
    )

    @Serializable
    data class Messages(
        val join: String = "<green><b>+</b></green> <name> <gray>joined the server</gray>",
        val leave: String = "<red><b>-</b></red> <name> <gray>left the server (online for <time_online>)</gray>",
        @SerializedName("first_time_join")
        val firstTimeJoin: String = "<color:AA00AA><b>✦ Welcome §f%name%§d to the server!</b></color>",
        val welcome: String = "<b>Welcome to PCB!</b>",
    )

    @Serializable
    data class Tab(
        val header: List<String> = emptyList(),
        val footer: List<String> = emptyList(),
        val player: String = "%name%",
        @SerializedName("player_column_length")
        val playerColumnLength: Int = 32,
    )

    @Serializable
    data class ServerLinks(
        val website: String = "",
        val custom: Map<String, String> = mapOf(),
    )

    @Serializable
    data class Hub(
        @SerializedName("world_id")
        val worldId: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float,
    )
}

@Serializable
data class RemoteConfigVersion(
    val version: Int,
    val config: RemoteConfigKeyValues,
)