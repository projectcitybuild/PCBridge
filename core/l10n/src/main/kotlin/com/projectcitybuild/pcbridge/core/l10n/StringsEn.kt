package com.projectcitybuild.pcbridge.core.l10n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Location
import kotlin.time.Duration

val l10n = StringsEn()

class StringsEn {
    private val miniMessage = MiniMessage.miniMessage()

    private fun mm(
        template: String,
        vararg resolvers: TagResolver,
    ): Component = miniMessage.deserialize(template, *resolvers)

    private fun Boolean.onOff(): String = if (this) "ON" else "OFF"

    private fun requiredField(label: String) = Component.text(label).append(Component.text("*", NamedTextColor.RED))

    val pagination = Pagination()
    val errors = Errors()
    val lists = Lists()
    val builds = Builds()
    val homes = Homes()
    val warps = Warps()
    val bans = Bans()
    val sync = Sync()
    val config = Config()
    val warnings = Warnings()
    val pim = Pim()
    val chatBadge = ChatBadge()
    val discord = Discord()
    val dialogs = Dialogs()
    val registration = Registration()
    val building = Building()
    val teleportation = Teleportation()
    val spawns = Spawns()
    val moderation = Moderation()
    val connection = Connection()
    val serverList = ServerList()
    val tablist = Tablist()
    val maintenance = Maintenance()
    val worlds = Worlds()

    inner class Pagination {
        val pagePrevButton = mm("<white>[← Prev]</white>")

        val pageNextButton = mm("<white>[Next →]</white>")

        val pagePrevButtonHover = Component.text("Click for previous page")

        val pageNextButtonHover = Component.text("Click for next page")

        val pageLabel = "Page"

        val errorPageNotFound = mm("<red>Page not found</red>")
    }

    inner class Errors {
        val errorOnlyPlayersCanUseThisCommand = mm("<red>Only players can use this command</red>")

        val errorNewNameCannotBeEmpty = mm("<red>Error: New name cannot be empty</red>")

        fun errorWithMessage(message: String?) = mm(
            "<red>Error: <message></red>",
            Placeholder.unparsed("message", message ?: "null"),
        )

        fun invalidArgumentValue(value: String) = Component.text("Invalid value: $value", NamedTextColor.RED)

        fun errorPlayerNotFound(name: String) = Component.text("Player $name not found", NamedTextColor.RED)

        fun commandError(message: String?) = mm(
            "<red><message></red>",
            Placeholder.unparsed("message", message ?: "null"),
        )

        fun commandErrorWithPrefix(message: String) = mm(
            "<red>Error: <message></red>",
            Placeholder.unparsed("message", message),
        )

        val commandErrorValidationFailed = commandErrorWithPrefix("Validation failed")

        val commandErrorNotFound = commandErrorWithPrefix("Not Found")

        val commandErrorNotPermitted = commandErrorWithPrefix("Not permitted to perform this action")

        val commandErrorConflict = commandErrorWithPrefix("Conflict")

        fun commandErrorCooldown(seconds: Long) = commandErrorWithPrefix("Please wait $seconds seconds before trying again")

        val commandUnexpectedError = commandError("An unexpected error occurred")

        fun errorWorldNotFound(name: String) = mm(
            "<red>Could not find world <name></red>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Lists {
        fun teleportTo(name: String) = Component.text("Teleport to $name")
    }

    inner class Builds {
        val errorNoBuildsFound = mm("<gray>No builds found</gray>")

        fun buildCreated(name: String) = mm(
            "<green><name> created</green>",
            Placeholder.unparsed("name", name),
        )

        fun buildDeleted(name: String) = mm(
            "<green><name> deleted</green>",
            Placeholder.unparsed("name", name),
        )

        fun buildMoved(name: String) = mm(
            "<green><name> moved to your location</green>",
            Placeholder.unparsed("name", name),
        )

        val buildUpdated = mm("<green>Build updated</green>")

        fun buildVoteRecorded(name: String) = mm(
            "<green>You voted for <name></green>",
            Placeholder.unparsed("name", name),
        )

        fun buildVoteRemoved(name: String) = mm(
            "<gray>You removed your vote for <name></gray>",
            Placeholder.unparsed("name", name),
        )

        fun buildCreatedAnnouncement(
            playerName: String,
            buildName: String,
        ) = mm(
            "<gray>[<red>✎</red>] <player> created build \"<build>\"</gray>",
            Placeholder.unparsed("player", playerName),
            Placeholder.component("build", clickableBuildName(buildName)),
        )

        fun buildVoteAnnouncement(
            playerName: String,
            buildName: String,
        ) = mm(
            "<gray>[<red>❤</red>] <player> voted for build \"<build>\"</gray>",
            Placeholder.unparsed("player", playerName),
            Placeholder.component("build", clickableBuildName(buildName)),
        )

        val buildListTitle = "Build List"

        fun buildListItem(
            id: Int,
            name: String,
            votes: Int,
        ) = mm(
            "<gray>#<id> \"<aqua><name></aqua>\" (<white><votes></white> votes)</gray>",
            Placeholder.unparsed("id", id.toString()),
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("votes", votes.toString()),
        )

        val clickBuildFieldToEdit = Component.text("Click a field to edit:", NamedTextColor.GRAY)

        fun buildCreatedBy(owner: String) = mm(
            "<gray>Created by <owner></gray>",
            Placeholder.unparsed("owner", owner),
        )

        fun buildTeleportedTo(name: String) = mm(
            "<gray>Teleported to <aqua><name></aqua></gray>",
            Placeholder.unparsed("name", name),
        )

        fun buildDescription(description: Component) = mm(
            "<gray>---<newline><description></gray>",
            Placeholder.component("description", description),
        )

        fun buildLore(lore: Component) = mm(
            "<gray>---<newline><italic><lore></italic></gray>",
            Placeholder.component("lore", lore),
        )

        private fun clickableBuildName(name: String) = Component
            .text(name, NamedTextColor.WHITE)
            .clickEvent(ClickEvent.runCommand("/build $name"))
            .hoverEvent(HoverEvent.showText(Component.text("Click to teleport")))
    }

    inner class Homes {
        val homesCannotCreate = mm("<gray>You cannot create a home</gray>")

        fun homesLimit(
            current: Int,
            max: Int,
        ) = mm(
            "You currently have <aqua><current></aqua> of <aqua><max></aqua> homes",
            Placeholder.unparsed("current", current.toString()),
            Placeholder.unparsed("max", max.toString()),
        )

        fun homesLimitRoles(roles: List<Pair<String, Int>>): Component {
            var roleLines: Component = Component.empty()
            roles.forEachIndexed { index, (role, homes) ->
                if (index > 0) roleLines = roleLines.append(Component.text(", "))
                roleLines =
                    roleLines.append(
                        mm(
                            "<newline><gray><role>: <aqua>+<homes></aqua> homes</gray>",
                            Placeholder.unparsed("role", role),
                            Placeholder.unparsed("homes", homes.toString()),
                        ),
                    )
            }
            return mm(
                "<gray>The following roles grant you homes:</gray> <roles>",
                Placeholder.component("roles", roleLines),
            )
        }

        val homesLimitDivider = mm("<gray>---</gray>")

        val homesListTitle = "Your Homes"

        fun homesListItem(
            name: String,
            world: String,
            x: Int,
            y: Int,
            z: Int,
        ) = mm(
            "<aqua><name></aqua> <gray>(<world>: <x>, <y>, <z>)</gray>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("world", world),
            Placeholder.unparsed("x", x.toString()),
            Placeholder.unparsed("y", y.toString()),
            Placeholder.unparsed("z", z.toString()),
        )

        val errorRenameHomeNotFound = mm("<red>Error: Home not found</red>")

        val renameHomeDialogTitle = Component.text("Rename Home")

        val homeIdInputLabel = Component.text("Home Id")

        val renameHomeButtonHover = Component.text("Click to rename the home")

        fun errorHomeNotFound(name: String) = mm(
            "<red>Could not find home <name></red>",
            Placeholder.unparsed("name", name),
        )

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
    }

    inner class Warps {
        val warpsListTitle = "Warps"

        fun warpListItem(
            name: String,
            world: String,
        ) = mm(
            "<gray>\"<aqua><u><name></u></aqua>\" (<world>)</gray>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("world", world),
        )

        val errorRenameWarpNotFound = mm("<red>Error: Warp not found</red>")

        val renameWarpDialogTitle = Component.text("Rename Warp")

        val warpIdInputLabel = Component.text("Warp Id")

        val renameWarpButtonHover = Component.text("Click to rename the warp")

        fun errorWarpNotFound(name: String) = mm(
            "<red>Could not find warp <name></red>",
            Placeholder.unparsed("name", name),
        )

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
    }

    inner class Bans {
        fun playerBannedAnnouncement(name: String) = mm(
            "<gray><name> has been banned</gray>",
            Placeholder.unparsed("name", name),
        )

        fun playerBanMessage(
            reason: Component?,
            expiresAt: String?,
        ) = mm(
            """
            <color:red><b>This account is banned.</b></color>

            <color:gray>Reason:</color> <reason>
            <color:gray>Expires:</color> <expires>

            <color:aqua>Appeal @ https://projectcitybuild.com"</color>
            """.trimIndent(),
            Placeholder.component("reason", reason ?: Component.text("No reason provided")),
            Placeholder.unparsed("expires", expiresAt ?: "Never"),
        )

        fun ipBanMessage(reason: Component?) = mm(
            """
            <color:red><b>This IP address is banned.</b></color>

            <color:gray>Reason:</color> <reason>

            <color:aqua>Appeal @ https://projectcitybuild.com</color>
            """.trimIndent(),
            Placeholder.component("reason", reason ?: Component.text("No reason provided")),
        )

        val createBanDialogTitle = Component.text("Ban Player")

        val defaultBanReason = "Griefing"

        val banPlayerNameInputLabel = requiredField("Player Name")

        val banReasonInputLabel = requiredField("Reason (shown to player)")

        val banAdditionalInfoInputLabel = Component.text("Additional Info/Context")

        val confirmBanButton = Component.text("Ban Player", NamedTextColor.RED)

        val confirmBanButtonHover = Component.text("Click to ban the player")

        fun playerHasBeenBanned(name: String) = mm(
            "<name> has been banned",
            Placeholder.unparsed("name", name),
        )

        fun clickToEditBan(url: String) = mm(
            "<gray>Click <click:OPEN_URL:'<url>'><hover:show_text:'<url>'><aqua><underlined>here</underlined></aqua></hover></click> to edit the ban or add additional information</gray>",
            Placeholder.unparsed("url", url),
        )
    }

    inner class Sync {
        val syncFetchingPlayerData = mm("<gray>Fetching player data...</gray>")

        fun syncFetchingPlayerDataFor(name: String) = mm(
            "<gray>Fetching player data for <name>...</gray>",
            Placeholder.unparsed("name", name),
        )

        val syncPlayerDataSynced = mm("<green>Player data synced</green>")

        val syncAccountSynced = mm("<green>Your account has been synced</green>")
    }

    inner class Config {
        val configFetching = mm("<gray>Fetching config...</gray>")

        val configReloaded = mm("<green>Remote config reloaded</green>")
    }

    inner class Warnings {
        val warningCreationInstructions = mm("<gray>Click the link below to create a warning for this player</gray>")

        fun warningCreationLink(url: String) = Component
            .text(url, NamedTextColor.AQUA)
            .decorate(TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.openUrl(url))
    }

    inner class Pim {
        private val opElevationAlreadyActivePrefix = "<red>Error: You are already OP elevated (remaining: <remaining>)</red>"

        fun opElevationAlreadyActive(remaining: String?) = mm(
            opElevationAlreadyActivePrefix,
            Placeholder.unparsed("remaining", remaining ?: "null"),
        )

        val opElevationNotActive = mm("<gray>You are not currently OP elevated</gray>")

        fun opElevationActive(remaining: String?) = mm(
            "<gray>You are OP elevated (remaining: <remaining>)</gray>",
            Placeholder.unparsed("remaining", remaining ?: "null"),
        )

        val errorOpElevationReasonRequired = mm("<red>Error: elevation reason is required</red>")

        fun rolesSet(roles: String) = mm(
            "<red>Your roles have been set to <roles></red><newline><gray>Use /sync or reconnect to revert this</gray>",
            Placeholder.unparsed("roles", roles),
        )

        val opElevationDialogTitle = Component.text("Warning")

        val opElevationWarning = mm(
            """
            #<yellow><b>You are about to receive temporary Operator (OP) privileges.</b></yellow>
            #
            #• <aqua>Full administrative permissions</aqua> will be granted.
            #• Your OP status will be <aqua>automatically revoked</aqua> after the set duration.
            #• You may revoke it early at any time with: <aqua><b>/pim op revoke</b></aqua>
            #
            #<dark_red><b>⚠ IMPORTANT</b></dark_red>
            #<i>
            #OP privileges bypass the normal permission system. They may override protections or cause unintended behavior.
            #
            #Please revoke your OP status as soon as your task is complete.
            #</i>
            """.trimMargin("#"),
        )

        val opElevationReasonInputLabel = requiredField("Reason for Elevation")

        val opElevationConfirmButton = Component.text("I understand.")

        val opElevationRevoked = mm("<gray><i>OP elevation was revoked</i></gray>")

        val opElevationExpired = mm("<gray><i>OP elevation expired</i></gray>")

        fun opElevationGranted(remainingTime: String) = mm(
            "<gray>OP status granted (remaining: <remaining>)</gray>",
            Placeholder.unparsed("remaining", remainingTime),
        )
    }

    inner class ChatBadge {
        val chatBadgeHeader = Component.text("Badges", NamedTextColor.YELLOW)

        val chatBadgeDivider = Component.text("---", NamedTextColor.GRAY)
    }

    inner class Discord {
        val discordSignEditedTitle = "Sign Edited"

        val discordBookEditedTitle = "Book Edited"

        val discordItemRenamedTitle = "Item Renamed"

        val discordLocationField = "Location"

        val discordTitleField = "Title"

        val discordBookAuthorField = "Book Author"

        val discordItemField = "Item"

        val discordItemQuantityField = "Item Quantity"

        val discordTextDiffSeparator = "---"

        fun discordTextDiff(
            oldText: String,
            newText: String,
        ) = "[OLD] $oldText\n[NEW] $newText"
    }

    inner class Dialogs {
        fun currentResourceName(name: String) = mm(
            "Current name: <aqua><name></aqua>",
            Placeholder.unparsed("name", name),
        )

        val newNameInputLabel = requiredField("New Name")

        val dialogSaveButton = Component.text("Save")

        val dialogCancelButton = Component.text("Cancel")

        val dialogCancelButtonGray = Component.text("Cancel", NamedTextColor.GRAY)

        val discardDialogInputHover = Component.text("Click to discard your input")
    }

    inner class Registration {
        val verifyRegistrationDialogTitle = Component.text("Enter Code")

        val registrationCodeInputLabel = requiredField("Code")

        val verifyRegistrationButton = Component.text("Verify")

        val errorNoRegisterEmailSpecified =
            mm(
                "<red>Please specify an email address to receive your registration code</red><newline><gray>Example Usage: <bold>/register your@email.com</bold></gray>",
            )

        val errorNoCodeSpecified = mm("<red>You did not specify a code</red>")

        val errorCodeInvalidOrExpired = mm("<red>Code is invalid or expired</red>")

        fun codeHasBeenEmailedTo(email: String) = mm(
            "A code has been emailed to <aqua><email></aqua>.<newline>Please type the code below to finish registration.",
            Placeholder.unparsed("email", email),
        )

        val codeHasBeenEmailed = mm("Please type in the code emailed to you to finish registration.")

        val registrationComplete = mm("<green>Registration complete! Your account will be synced momentarily...</green>")
    }

    inner class Building {
        val errorNoItemToRename = mm("<red>No item in hand to rename</red>")

        val nightVisionToggledOn = mm("<gray><i>NightVision toggled on</i></gray>")

        val nightVisionToggledOff = mm("<gray><i>NightVision toggled off</i></gray>")

        val receivedInvisFrame = mm("<gray><i>You received an invisible item frame</i></gray>")

        val receivedInvisFrameGlowing = mm("<gray><i>You received an invisible glowing frame</i></gray>")

        fun renamedItem(newName: TextComponent) = mm(
            "<gray>Renamed item in hand to <name></gray>",
            Placeholder.component("name", newName),
        )
    }

    inner class Teleportation {
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

        val errorTeleportCoordinatesInvalid = mm("<red>Coordinates must be finite numbers</red>")

        val errorTeleportExplicitTargetRequired = mm("<red>Specify an online destination player</red>")

        val searchingForSafeLocation = mm("<gray><i>Searching for a safe location...</i></gray>")

        fun teleportRequestReceived(name: String) = mm(
            "<yellow><name> wants to teleport to you. Use <click:run_command:'/tp accept'><hover:show_text:'Click to accept'>/tp accept</hover></click> or <click:run_command:'/tp deny'><hover:show_text:'Click to deny'>/tp deny</hover></click>.</yellow>",
            Placeholder.unparsed("name", name),
        )

        fun teleportHereRequestReceived(name: String) = mm(
            "<yellow><name> wants you to teleport to them. Use <click:run_command:'/tp accept'><hover:show_text:'Click to accept'>/tp accept</hover></click> or <click:run_command:'/tp deny'><hover:show_text:'Click to deny'>/tp deny</hover></click>.</yellow>",
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

        fun teleportRequestCreated(
            name: String,
            ttl: Duration,
        ) = mm(
            "<green>Teleport request to <name> created (expires in ${ttl.inWholeSeconds} seconds)</green>",
            Placeholder.unparsed("name", name),
        )

        fun teleportCompleted(name: String) = mm(
            "<green>Teleported to <name></green>",
            Placeholder.unparsed("name", name),
        )

        fun teleportOperationSucceeded(
            player: String,
            target: String,
        ) = mm(
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

        fun teleportBulkSummary(
            succeeded: Int,
            failed: Int,
        ) = mm(
            "<green>Teleported <succeeded> player(s)</green><gray>; <failed> failed</gray>",
            Placeholder.unparsed("succeeded", succeeded.toString()),
            Placeholder.unparsed("failed", failed.toString()),
        )

        fun teleportRequestsCreated(count: Int) = mm(
            "<green>Teleport requests sent to <count> player(s)</green>",
            Placeholder.unparsed("count", count.toString()),
        )

        fun teleportedToCoordinate(
            x: Double,
            y: Double,
            z: Double,
        ) = mm(
            "<gray><i>⚡ Teleported to <b>{<x>, <y>, <z>}</b></i></gray>",
            Placeholder.unparsed("x", x.toString()),
            Placeholder.unparsed("y", y.toString()),
            Placeholder.unparsed("z", z.toString()),
        )

        fun teleportedToName(name: String) = mm(
            "<gray><i>⚡ Teleported to <b><name></b></i></gray>",
            Placeholder.unparsed("name", name),
        )

        fun teleportedToCoordinate(
            x: Int,
            y: Int,
            z: Int,
        ) = mm(
            "<gray><i>⚡ Teleported to <b>{<x>, <y>, <z>}</b></i></gray>",
            Placeholder.unparsed("x", x.toString()),
            Placeholder.unparsed("y", y.toString()),
            Placeholder.unparsed("z", z.toString()),
        )
    }

    inner class Spawns {
        val errorHubWorldNotFound = mm("<red>Could not find hub world</red>")

        val errorHubNotSet = mm("<red>Hub world not set</red>")

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

        val teleportedToSpawn = mm("<gray><i>⚡ Teleported to spawn</i></gray>")

        val teleportedToHub = mm("<gray><i>⚡ Teleported to hub</i></gray>")
    }

    inner class Moderation {
        val kickedFromServer = mm("Kicked from server.")

        fun playerWasKickedForReason(
            name: String,
            reason: String,
        ) = mm(
            "<i><gray><name> was kicked: <reason></gray></i>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("reason", reason),
        )

        fun playerWasKicked(name: String) = mm(
            "<i><gray><name> was kicked</gray></i>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Connection {
        val errorUnableToAuthConnection = Component.text("Unable to authorize your connection. Please try again later.")

        val playerStateUnavailableWarning =
            Component.text(
                "Warning: Unable to retrieve player data. Your permissions, rank and other data may be missing. Please type /sync or try reconnect in a moment",
            )
    }

    inner class ServerList {
        val defaultServerListMotd = Component.text("Project City Build")
    }

    inner class Tablist {
        fun playerPing(ping: Int) = mm(
            " <gray><ping></gray>",
            Placeholder.unparsed("ping", ping.toString()),
        )
    }

    inner class Maintenance {
        val maintenanceConnectionDenied =
            mm("<bold>Server Maintenance</bold><newline><newline><gray>Please try again later</gray>")

        val maintenanceMotd = mm("<red>Server maintenance - be right back!</red>")

        val maintenanceReminder = mm("<italic><yellow>[Reminder] Maintenance mode is currently ON")

        fun maintenanceAlreadyInState(enabled: Boolean) = mm(
            "<red>Maintenance mode is already <state></red>",
            Placeholder.unparsed("state", enabled.onOff()),
        )

        fun maintenanceModeChanged(enabled: Boolean) = mm(
            "<yellow>Maintenance mode is now <state></yellow>",
            Placeholder.unparsed("state", enabled.onOff()),
        )

        fun maintenanceStatus(enabled: Boolean) = mm(
            "Maintenance mode is currently <state>",
            Placeholder.unparsed("state", enabled.onOff()),
        )
    }

    inner class Worlds {
        val worldsCommandDescription = "Manages world settings"
        val borderCommandDescription = "Sets and enforces a world's border"
        val borderSetCommandDescription = "Sets a square, circle, or rectangle border with an optional center"
        val borderFillCommandDescription = "Generates missing chunks inside the world border"
        val borderClearCommandDescription = "Clears the world's border"

        val borderNotConfigured = mm("<red>No world border is configured for this world.</red>")
        val borderCleared = mm("<green>World border cleared.</green>")

        fun borderStatus(
            shape: String,
            centerX: Double,
            centerZ: Double,
            radius: Double,
        ) = mm(
            "<gray>World border: <shape>, center <x>, <z>; radius <radius> blocks.</gray>",
            Placeholder.unparsed("shape", shape),
            Placeholder.unparsed("x", centerX.toString()),
            Placeholder.unparsed("z", centerZ.toString()),
            Placeholder.unparsed("radius", radius.toString()),
        )

        fun rectangleBorderStatus(
            centerX: Double,
            centerZ: Double,
            width: Double,
            depth: Double,
        ) = mm(
            "<gray>World border: rectangle, center <x>, <z>; width <width>, depth <depth> blocks.</gray>",
            Placeholder.unparsed("x", centerX.toString()),
            Placeholder.unparsed("z", centerZ.toString()),
            Placeholder.unparsed("width", width.toString()),
            Placeholder.unparsed("depth", depth.toString()),
        )

        fun invalidBorder(reason: String?) = mm(
            "<red>Invalid world border: <reason></red>",
            Placeholder.unparsed("reason", reason ?: "unknown reason"),
        )

        fun borderSet(
            shape: String,
            centerX: Double,
            centerZ: Double,
            radius: Double,
        ) = mm(
            "<green>World border set: <shape>, center <x>, <z>; radius <radius> blocks.</green>",
            Placeholder.unparsed("shape", shape),
            Placeholder.unparsed("x", centerX.toString()),
            Placeholder.unparsed("z", centerZ.toString()),
            Placeholder.unparsed("radius", radius.toString()),
        )

        fun rectangleBorderSet(
            centerX: Double,
            centerZ: Double,
            width: Double,
            depth: Double,
        ) = mm(
            "<green>World border set: rectangle, center <x>, <z>; width <width>, depth <depth> blocks.</green>",
            Placeholder.unparsed("x", centerX.toString()),
            Placeholder.unparsed("z", centerZ.toString()),
            Placeholder.unparsed("width", width.toString()),
            Placeholder.unparsed("depth", depth.toString()),
        )

        val fillRequiresBorder = mm("<red>Set a world border before filling chunks.</red>")

        fun fillPreview(
            chunkCount: Long,
            worldName: String,
        ) = mm(
            "<green>Fill preview: up to <count> chunks may be checked in <world>. " +
                "Only missing chunks will be generated. Run /worlds border fill confirm within 60 seconds to continue.</green>",
            Placeholder.unparsed("count", chunkCount.toString()),
            Placeholder.unparsed("world", worldName),
        )

        val fillPreviewMissing = mm("<red>There is no active fill preview. Run /worlds border fill first.</red>")
        val fillWrongWorld = mm("<red>You must confirm the fill in the world where it was previewed.</red>")
        val fillBorderChanged =
            mm(
                "<red>The world border changed. Run /worlds border fill again to preview the updated area.</red>",
            )
        val fillAlreadyRunning = mm("<red>A world border fill is already running for this world.</red>")
        val fillStarted = mm("<yellow>Starting world border fill...</yellow>")
        val fillStoppedBorderChanged =
            mm("<red>World border changed during fill; the operation was stopped.</red>")

        fun fillProgress(
            checked: Long,
            generated: Long,
        ) = mm(
            "<yellow>World border fill progress: checked <checked> chunks; generated <generated>.</yellow>",
            Placeholder.unparsed("checked", checked.toString()),
            Placeholder.unparsed("generated", generated.toString()),
        )

        fun fillComplete(
            checked: Long,
            generated: Long,
        ) = mm(
            "<green>World border fill complete: checked <checked> chunks; generated <generated>.</green>",
            Placeholder.unparsed("checked", checked.toString()),
            Placeholder.unparsed("generated", generated.toString()),
        )

        val fillFailed = mm("<red>World border fill failed. See the server log for details.</red>")
    }
}
