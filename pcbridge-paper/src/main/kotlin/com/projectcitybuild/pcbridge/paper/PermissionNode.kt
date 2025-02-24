package com.projectcitybuild.pcbridge.paper

enum class PermissionNode(val node: String) {
    // Can ban a player
    BANS_MANAGE("pcbridge.bans.manage"),

    // Can teleport to a build
    BUILDS_TELEPORT("pcbridge.builds.teleport"),

    // Can create, edit or delete a build
    BUILDS_MANAGE("pcbridge.builds.manage"),

    // Can vote for a build
    BUILDS_VOTE("pcbridge.builds.vote"),

    // Can toggle nightvision mode
    BUILDING_NIGHT_VISION("pcbridge.build.nightvision"),

    // Can rename an item via a command
    BUILDING_ITEM_RENAME("pcbridge.items.rename"),

    // Can get an invisible item frame via a command
    BUILDING_INVIS_FRAME("pcbridge.build.invisframe"),

    // Can create/edit/get their own player homes
    HOMES_USE("pcbridge.homes.use"),

    // Can send and receive staff messages
    STAFF_CHANNEL("pcbridge.chat.staff_channel"),

    // Can force sync another player (in addition to yourself)
    PLAYER_SYNC_OTHER("pcbridge.sync.other"),

    // Can use the syncdebug command
    PLAYER_SYNC_DEBUG("pcbridge.sync.debug"),

    // Can force reload the remote config
    REMOTE_CONFIG_RELOAD("pcbridge.config.reload"),

    // Can teleport to a warp
    WARP_TELEPORT("pcbridge.warp.teleport"),

    // Can create, edit or delete warps
    WARP_MANAGE("pcbridge.warp.manage"),

    // Can warn a player
    WARNINGS_MANAGE("pcbridge.warnings.manage"),
}