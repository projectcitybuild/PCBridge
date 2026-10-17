package com.projectcitybuild.pcbridge.core.l10n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Location

val l10n = StringsEn()

class StringsEn {
    private val miniMessage = MiniMessage.miniMessage()

    private fun mm(
        template: String,
        vararg resolvers: TagResolver,
    ): Component = miniMessage.deserialize(template, *resolvers)

    val pagePrevButton = mm("<white>[← Prev]</white>")

    val pageNextButton = mm("<white>[Next →]</white>")

    val pagePrevButtonHover = Component.text("Click for previous page")

    val pageNextButtonHover = Component.text("Click for next page")

    val errorPageNotFound = mm("<red>Page not found</red>")

    val errorOnlyPlayersCanUseThisCommand = mm("<red>Only players can use this command</red>")

    val errorNoBuildsFound = mm("<gray>No builds found</gray>")

    fun errorWorldNotFound(name: String) = mm(
        "<red>Could not find world <name></red>",
        Placeholder.unparsed("name", name),
    )

    fun errorWarpNotFound(name: String) = mm(
        "<red>Could not find warp <name></red>",
        Placeholder.unparsed("name", name),
    )

    fun errorHomeNotFound(name: String) = mm(
        "<red>Could not find home <name></red>",
        Placeholder.unparsed("name", name),
    )

    val errorNoItemToRename = mm("<red>No item in hand to rename</red>")

    val errorCouldNotFindSafeLocation = mm("<red>Failed to find a safe location</red>")

    val errorTeleportPermission = mm("<red>You do not have permission to use this teleport option</red>")

    val errorTeleportSelf = mm("<red>You cannot teleport to yourself</red>")

    val errorTeleportRequestPending = mm("<red>You already have an outgoing teleport request</red>")

    val errorTeleportRequestMissing = mm("<red>No matching teleport request was found</red>")

    val errorTeleportRequestAmbiguous = mm("<red>You have multiple incoming requests; specify a player</red>")

    val errorTeleportRequestExpired = mm("<red>That teleport request has expired</red>")

    val errorTeleportHistoryMissing = mm("<red>No previous location is available</red>")

    val errorTeleportHistoryWorldUnavailable = mm("<red>The world for your previous location is unavailable</red>")

    val errorTeleportFailed = mm("<red>The teleport failed</red>")

    val errorTeleportTargetOffline = mm("<red>That player is no longer online</red>")

    val errorTeleportWorldUnavailable = mm("<red>That world is unavailable</red>")

    val errorTeleportCoordinatesInvalid = mm("<red>Coordinates must be finite numbers</red>")

    val errorTeleportExplicitTargetRequired = mm("<red>Specify an online destination player</red>")

    val errorNoRegisterEmailSpecified = mm("<red>Please specify an email address to receive your registration code</red><newline><gray>Example Usage: <bold>/register your@email.com</bold></gray>")

    val errorNoCodeSpecified = mm("<red>You did not specify a code</red>")

    val errorCodeInvalidOrExpired = mm("<red>Code is invalid or expired</red>")

    val errorHubWorldNotFound = mm("<red>Could not find hub world</red>")

    val errorHubNotSet = mm("<red>Hub world not set</red>")

    val errorUnableToAuthConnection = Component.text("Unable to authorize your connection. Please try again later.")

    fun homeCreated(name: String) = mm(
        "<green><name> created</green>",
        Placeholder.unparsed("name", name),
    )

    fun homeDeleted(name: String) = mm(
        "<green><name> deleted</green>",
        Placeholder.unparsed("name", name),
    )

    fun homeMoved(name: String) = mm(
        "<green><name> moved to your location</green>",
        Placeholder.unparsed("name", name),
    )

    fun homeRenamed(newName: String) = mm(
        "<green>Home renamed to <aqua><name></aqua></green>",
        Placeholder.unparsed("name", newName),
    )

    val noHomesFound = mm("<gray>No homes found</gray>")

    fun warpCreated(name: String) = mm(
        "<green><name> created</green>",
        Placeholder.unparsed("name", name),
    )

    fun warpDeleted(name: String) = mm(
        "<green><name> deleted</green>",
        Placeholder.unparsed("name", name),
    )

    fun warpMoved(name: String) = mm(
        "<green><name> moved to your location</green>",
        Placeholder.unparsed("name", name),
    )

    fun warpRenamed(newName: String) = mm(
        "<green>Warp renamed to <aqua><name></aqua></green>",
        Placeholder.unparsed("name", newName),
    )

    val noWarpsFound = mm("<gray>No warps found</gray>")

    val nightVisionToggledOn = mm("<gray><i>NightVision toggled on</i></gray>")

    val nightVisionToggledOff = mm("<gray><i>NightVision toggled off</i></gray>")

    val receivedInvisFrame = mm("<gray><i>You received an invisible item frame</i></gray>")

    val receivedInvisFrameGlowing = mm("<gray><i>You received an invisible glowing frame</i></gray>")

    fun renamedItem(newName: TextComponent) = mm(
        "<gray>Renamed item in hand to <name></gray>",
        Placeholder.component("name", newName),
    )

    fun codeHasBeenEmailedTo(email: String) = mm(
        "A code has been emailed to <aqua><email></aqua>.<newline>Please type the code below to finish registration.",
        Placeholder.unparsed("email", email),
    )

    val codeHasBeenEmailed = mm("Please type in the code emailed to you to finish registration.")

    fun spawnSet(location: Location) = mm(
        "<green>World spawn set to <gray>" +
                "{<x>, <y>, <z>} pitch=<pitch> yaw=<yaw>" +
                "</gray></green>",
        Placeholder.unparsed("x", location.x.toString()),
        Placeholder.unparsed("y", location.y.toString()),
        Placeholder.unparsed("z", location.z.toString()),
        Placeholder.unparsed("pitch", location.pitch.toString()),
        Placeholder.unparsed("yaw", location.yaw.toString()),
    )

    val searchingForSafeLocation = mm("<gray><i>Searching for a safe location...</i></gray>")

    val teleportCommandHelp = mm(
        "<yellow>/tp [player]|to|here|send|all|pos|back|accept|deny|cancel</yellow>",
    )

    fun teleportRequestSent(name: String) = mm(
        "<green>Teleport request sent to <name></green>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestReceived(name: String) = mm(
        "<yellow><name> wants to teleport to you. Use /tp accept [player] or /tp deny [player].</yellow>",
        Placeholder.unparsed("name", name),
    )

    fun teleportHereRequestReceived(name: String) = mm(
        "<yellow><name> wants you to teleport to them. Use /tp accept [player] or /tp deny [player].</yellow>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestAccepted(name: String) = mm(
        "<green>Teleport request from <name> accepted</green>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestDenied(name: String) = mm(
        "<red>Teleport request from <name> denied</red>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestWasDenied(name: String) = mm(
        "<red><name> denied your teleport request</red>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestCancelled(name: String) = mm(
        "<yellow>Teleport request to <name> cancelled</yellow>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestWasCancelled(name: String) = mm(
        "<yellow><name> cancelled their teleport request</yellow>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestExpired(name: String) = mm(
        "<yellow>Teleport request from <name> expired</yellow>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestSentExpired(name: String) = mm(
        "<yellow>Your teleport request to <name> expired</yellow>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestDisconnected(name: String) = mm(
        "<yellow>Teleport request involving <name> was removed because a player disconnected</yellow>",
        Placeholder.unparsed("name", name),
    )

    fun teleportRequestCreated(name: String) = mm(
        "<green>Teleport request to <name> created (expires in 60 seconds)</green>",
        Placeholder.unparsed("name", name),
    )

    fun teleportCompleted(name: String) = mm(
        "<green>Teleported to <name></green>",
        Placeholder.unparsed("name", name),
    )

    fun teleportOperationSucceeded(player: String, target: String) = mm(
        "<green>Teleported <player> to <target></green>",
        Placeholder.unparsed("player", player),
        Placeholder.unparsed("target", target),
    )

    fun teleportReceived(name: String) = mm(
        "<gray><name> teleported to you</gray>",
        Placeholder.unparsed("name", name),
    )

    fun teleportBackCompleted(name: String) = mm(
        "<green><name> returned to their previous location</green>",
        Placeholder.unparsed("name", name),
    )

    fun teleportBulkSummary(succeeded: Int, failed: Int) = mm(
        "<green>Teleported <succeeded> player(s)</green><gray>; <failed> failed</gray>",
        Placeholder.unparsed("succeeded", succeeded.toString()),
        Placeholder.unparsed("failed", failed.toString()),
    )

    fun teleportRequestsCreated(count: Int) = mm(
        "<green>Teleport requests sent to <count> player(s)</green>",
        Placeholder.unparsed("count", count.toString()),
    )

    fun teleportedToCoordinate(x: Double, y: Double, z: Double) = mm(
        "<gray><i>⚡ Teleported to <b>{<x>, <y>, <z>}</b></i></gray>",
        Placeholder.unparsed("x", x.toString()),
        Placeholder.unparsed("y", y.toString()),
        Placeholder.unparsed("z", z.toString()),
    )

    fun teleportedToName(name: String) = mm(
        "<gray><i>⚡ Teleported to <b><name></b></i></gray>",
        Placeholder.unparsed("name", name),
    )

    fun teleportedToCoordinate(x: Int, y: Int, z: Int) = mm(
        "<gray><i>⚡ Teleported to <b>{<x>, <y>, <z>}</b></i></gray>",
        Placeholder.unparsed("x", x.toString()),
        Placeholder.unparsed("y", y.toString()),
        Placeholder.unparsed("z", z.toString()),
    )

    val teleportedToSpawn = mm("<gray><i>⚡ Teleported to spawn</i></gray>")

    val teleportedToHub = mm("<gray><i>⚡ Teleported to hub</i></gray>")

    fun playerHasBeenBanned(name: String) = mm(
        "<name> has been banned",
        Placeholder.unparsed("name", name),
    )

    fun clickToEditBan(url: String) = mm(
        "<gray>Click <click:OPEN_URL:'<url>'><hover:show_text:'<url>'><aqua><underlined>here</underlined></aqua></hover></click> to edit the ban or add additional information</gray>",
        Placeholder.unparsed("url", url),
    )

    val registrationComplete = mm("<green>Registration complete! Your account will be synced momentarily...</green>")

    val kickedFromServer = mm("Kicked from server.")

    fun playerWasKickedForReason(name: String, reason: String) = mm(
        "<i><gray><name> was kicked: <reason></gray></i>",
        Placeholder.unparsed("name", name),
        Placeholder.unparsed("reason", reason),
    )

    fun playerWasKicked(name: String) = mm(
        "<i><gray><name> was kicked</gray></i>",
        Placeholder.unparsed("name", name),
    )

    val opElevationRevoked = mm("<gray><i>OP elevation was revoked</i></gray>")

    val opElevationExpired = mm("<gray><i>OP elevation expired</i></gray>")

    fun opElevationGranted(remainingTime: String) = mm(
        "<gray>OP status granted (remaining: <remaining>)</gray>",
        Placeholder.unparsed("remaining", remainingTime),
    )
}
