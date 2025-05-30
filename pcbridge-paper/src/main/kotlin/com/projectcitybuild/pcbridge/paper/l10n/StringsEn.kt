package com.projectcitybuild.pcbridge.paper.l10n

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
    val errorNoCodeSpecified = "<red>You did not specify a code</red><newline><gray>Example Usage: <bold>/code 123456</bold></gray>"
    val errorCodeInvalidOrExpired = "<red>Code is invalid or expired</red>"
    val errorHubWorldNotFound = "<red>Could not find hub world</red>"
    val errorSecondsMustBeGreaterThanZero = "<red>Seconds must be greater than 0</red>"
    val errorPlayerNotOnFire = { name: String -> "<red>$name is not on fire</red>" }
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
    val codeHasBeenEmailed = { email:String -> "<gray>A code has been emailed to $email.<newline>Please type it in with <aqua><bold><hover:show_text:'/code'><click:suggest_command:/code >/code [code]</click></hover></bold></aqua></gray>" }
    val spawnSet = { location: Location -> "<green>Set the world spawn point to <gray>${location.x} ${location.y} ${location.z} ${location.pitch} ${location.yaw}</gray></green>" }
    val searchingForSafeLocation = "<gray><i>Searching for a safe location...</i></gray>"
    val teleportedToName = { name: String -> "<gray><i>⚡ Teleported to <b>$name</b></i></gray>" }
    val teleportedToCoordinate = { x: Int, y: Int, z: Int -> "<gray><i>⚡ Teleported to <b>x=$x, y=$y, z=$z</b></gray></i>" }
    val teleportedToSpawn = "<gray><i>⚡ Teleported to spawn</i></gray>"
    val teleportedToHub = "<gray><i>⚡ Teleported to hub</i></gray>"
    val youHaveBeenBurned = { seconds: Double -> "<gray>You have been set on fire for $seconds seconds</gray>" }
    val burnedPlayer = { name: String, seconds: Double, ticks: Int -> "<gray><i>Set <white>$name</white> on fire for <white>${seconds}</white>s ($ticks ticks)</i></gray>" }
    val youHaveBeenIced = { seconds: Double -> "<gray>You have been iced for $seconds seconds</gray>" }
    val icedPlayer = { name: String, seconds: Double, ticks: Int -> "<gray><i>Iced <white>$name</white> for <white>${seconds}</white>s ($ticks ticks)</i></gray>" }
    val youHaveBeenExtinguished = "<gray>You have been extinguished</gray>"
    val extinguishedPlayer = { name: String -> "<gray><i>Extinguished <white>$name</white></i></gray>" }
    val yourHungerHasBeenReset = "<gray>Your hunger and exhaustion has been reset</gray>"
    val fedPlayer = { name: String -> "<gray><i>Hunger and exhaustion reset for <white>$name</white></i></gray>" }
    val youWereKilledBy = { killer: String -> "<gray>You were killed by $killer</gray>" }
    val killedPlayer = { name: String -> "<gray><i>Killed <white>$name</white></i></gray>" }
    val youHaveBeenHealed = "<gray>You have been healed"
    val healedPlayer = { name: String -> "<gray><i>Healed <white>$name</white></i></gray>" }
}