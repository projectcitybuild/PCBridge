package com.projectcitybuild

class Permissions private constructor() {
    companion object {
        const val COMMAND_BUILD_NIGHTVISION = "pcbridge.build.nightvision"
        const val COMMAND_BUILD_INVIS_FRAME = "pcbridge.build.invisframe"
        const val COMMAND_CHAT_TOGGLE_BADGE = "pcbridge.chat.badge"
        const val COMMAND_BANS_BAN = "pcbridge.ban.ban"
        const val COMMAND_BANS_BAN_IP = "pcbridge.ban.banip"
        const val COMMAND_BANS_CHECK_BAN = "pcbridge.ban.checkban"
        const val COMMAND_BANS_UNBAN = "pcbridge.ban.unban"
        const val COMMAND_BANS_UNBAN_IP = "pcbridge.ban.unbanip"
    }
}