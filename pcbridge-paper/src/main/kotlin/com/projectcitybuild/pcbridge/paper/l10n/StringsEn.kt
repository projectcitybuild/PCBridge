package com.projectcitybuild.pcbridge.paper.l10n

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
    val homeCreated = { name: String -> "<green>$name created</green>" }
    val homeDeleted = { name: String -> "<green>$name deleted</green>" }
    val homeMoved = { name: String -> "<green>$name moved to your location</green>" }
    val homeRenamed = { newName: String -> "<green>Home renamed to <aqua>$newName</aqua></green>" }
    val noHomesFound = "<gray>No homes found</gray>"
    val teleportedToName = { name: String -> "<gray><i>Teleported to <b>$name</b></i></gray>" }
}