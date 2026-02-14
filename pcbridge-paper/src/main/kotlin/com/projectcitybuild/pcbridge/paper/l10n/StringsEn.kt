package com.projectcitybuild.pcbridge.paper.l10n

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import org.bukkit.Location

val l10n = StringsEn()

class StringsEn {
    val pagePrevButton = "<white>[← Prev]</white>"
    val pageNextButton = "<white>[Next →]</white>"
    val pagePrevButtonHover = "Click for previous page"
    val pageNextButtonHover = "Click for next page"

    val errorPageNotFound = "<red>Page not found</red>"
    val errorOnlyPlayersCanUseThisCommand = "<red>Only players can use this command</red>"
    val errorWorldNotFound = { name: String -> "<red>Could not find world $name</red>" }
    val errorWarpNotFound = { name: String -> "<red>Could not find warp $name</red>" }
    val errorHomeNotFound = { name: String -> "<red>Could not find home $name</red>" }
    val errorNoItemToRename = "<red>No item in hand to rename</red>"
    val errorCouldNotFindSafeLocation = "<red>Failed to find a safe location</red>"
    val errorNoRegisterEmailSpecified = "<red>Please specify an email address to receive your registration code</red><newline><gray>Example Usage: <bold>/register your@email.com</bold></gray>"
    val errorNoCodeSpecified = "<red>You did not specify a code</red>"
    val errorCodeInvalidOrExpired = "<red>Code is invalid or expired</red>"
    val errorHubWorldNotFound = "<red>Could not find hub world</red>"
    val errorHubNotSet = "<red>Hub world not set</red>"
    val homeCreated = { name: String -> "<green>$name created</green>" }
    val homeDeleted = { name: String -> "<green>$name deleted</green>" }
    val homeMoved = { name: String -> "<green>$name moved to your location</green>" }
    val homeRenamed = { newName: String -> "<green>Home renamed to <aqua>$newName</aqua></green>" }
    val noHomesFound = "<gray>No homes found</gray>"
    val warpCreated = { name: String -> "<green>$name created</green>" }
    val warpDeleted = { name: String -> "<green>$name deleted</green>" }
    val warpMoved = { name: String -> "<green>$name moved to your location</green>" }
    val warpRenamed = { newName: String -> "<green>Warp renamed to <aqua>$newName</aqua></green>" }
    val noWarpsFound = "<gray>No warps found</gray>"
    val nightVisionToggledOn = "<gray><i>NightVision toggled on</i></gray>"
    val nightVisionToggledOff = "<gray><i>NightVision toggled off</i></gray>"
    val receivedInvisFrame = "<gray><i>You received an invisible item frame</i></gray>"
    val receivedInvisFrameGlowing = "<gray><i>You received an invisible glowing frame</i></gray>"
    val renamedItem = { name: String -> "<gray>Renamed item in hand to $name</gray>" }
    val codeHasBeenEmailedTo = { email: String -> "A code has been emailed to <aqua>$email</aqua>.<newline>Please type the code below to finish registration." }
    val codeHasBeenEmailed = "Please type in the code emailed to you to finish registration."
    val spawnSet = { location: Location -> "<green>World spawn set to <gray>{${location.x}, ${location.y}, ${location.z}} pitch=${location.pitch} yaw=${location.yaw}</gray></green>" }
    val searchingForSafeLocation = "<gray><i>Searching for a safe location...</i></gray>"
    val teleportedToName = { name: String -> "<gray><i>⚡ Teleported to <b>$name</b></i></gray>" }
    val teleportedToCoordinate = { x: Int, y: Int, z: Int -> "<gray><i>⚡ Teleported to <b>{$x, $y, $z}</b></i></gray>" }
    val teleportedToSpawn = "<gray><i>⚡ Teleported to spawn</i></gray>"
    val teleportedToHub = "<gray><i>⚡ Teleported to hub</i></gray>"
    val playerHasBeenBanned = { name: String -> "$name has been banned" }
    val clickToEditBan = { url: String -> "<gray>Click <click:OPEN_URL:'$url'><hover:show_text:'$url'><aqua><underlined>here</underlined></aqua></hover></click> to edit the ban or add additional information</gray>" }
    val registrationComplete = "<green>Registration complete! Your account will be synced momentarily...</green>"
    val kickedFromServer = "Kicked from server."
    val playerWasKickedForReason = { name: String, reason: String -> "<i><gray>$name was kicked: $reason</gray></i>" }
    val playerWasKicked = { name: String -> "<i><gray>$name was kicked</gray></i>" }
    val opElevationRevoked = "<gray><i>OP elevation was revoked</i></gray>"
    val opElevationExpired = "<gray><i>OP elevation expired</i></gray>"
    val opElevationGranted = { remaining: String -> "<gray>OP status granted (remaining: $remaining)</gray>" }
}