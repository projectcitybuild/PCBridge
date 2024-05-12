package com.projectcitybuild.pcbridge

class Permissions private constructor() {
    companion object {
        const val COMMAND_BUILD_NIGHTVISION = "pcbridge.build.nightvision"
        const val COMMAND_BUILD_INVIS_FRAME = "pcbridge.build.invisframe"
        const val COMMAND_BUILD_BIN = "pcbridge.build.bin"
        const val COMMAND_CHAT_TOGGLE_BADGE = "pcbridge.chat.badge"
        const val COMMAND_BANS_BAN = "pcbridge.ban.ban"
        const val COMMAND_BANS_BAN_IP = "pcbridge.ban.banip"
        const val COMMAND_BANS_CHECK_BAN = "pcbridge.ban.checkban"
        const val COMMAND_BANS_UNBAN = "pcbridge.ban.unban"
        const val COMMAND_BANS_UNBAN_IP = "pcbridge.ban.unbanip"
        const val COMMAND_MUTES_MUTE = "pcbridge.chat.mute"
        const val COMMAND_MUTES_UNMUTE = "pcbridge.chat.unmute"
        const val COMMAND_STAFF_CHAT = "pcbridge.chat.staff_channel"
        const val COMMAND_WARNING_ACKNOWLEDGE = "pcbridge.warning.acknowledge"
        const val COMMAND_RANKSYNC_SYNC = "pcbridge.sync.login"
        const val COMMAND_RANKSYNC_SYNC_OTHER = "pcbridge.sync.other"
        const val COMMAND_WARPS_DELETE = "pcbridge.warp.delete"
        const val COMMAND_WARPS_CREATE = "pcbridge.warp.create"
        const val COMMAND_WARPS_LIST = "pcbridge.warp.list"
        const val COMMAND_WARPS_USE = "pcbridge.warp.use"
        const val COMMAND_UTILITIES = "pcbridge.utilities"
    }
}