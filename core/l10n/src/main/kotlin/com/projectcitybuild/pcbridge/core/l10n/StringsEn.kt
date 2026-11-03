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
    val gameplay = Gameplay()
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
            "<red>Error: <aqua><message></aqua></red>",
            Placeholder.unparsed("message", message ?: "Unknown error"),
        )

        fun invalidArgumentValue(value: String) = mm(
            "<red>Invalid value: <aqua><value></aqua></red>",
            Placeholder.unparsed("value", value),
        )

        fun errorPlayerNotFound(name: String) = mm(
            "<red>Player <aqua><name></aqua> not found</red>",
            Placeholder.unparsed("name", name),
        )

        fun commandError(message: String?) = mm(
            "<red><aqua><message></aqua></red>",
            Placeholder.unparsed("message", message ?: "Unknown error"),
        )

        fun commandErrorWithPrefix(message: String) = mm(
            "<red>Error: <aqua><message></aqua></red>",
            Placeholder.unparsed("message", message),
        )

        val commandErrorValidationFailed = commandErrorWithPrefix("Validation failed")

        val commandErrorNotFound = commandErrorWithPrefix("Not Found")

        val commandErrorNotPermitted = commandErrorWithPrefix("Not permitted to perform this action")

        val commandErrorConflict = commandErrorWithPrefix("Conflict")

        fun commandErrorCooldown(seconds: Long) = mm(
            "<red>Error: Please wait <aqua><seconds></aqua> seconds before trying again</red>",
            Placeholder.unparsed("seconds", seconds.toString()),
        )

        val commandUnexpectedError = commandError("An unexpected error occurred")

        fun errorWorldNotFound(name: String) = mm(
            "<red>Could not find world <aqua><name></aqua></red>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Lists {
        fun teleportTo(name: String) = mm(
            "Teleport to <aqua><name></aqua>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Builds {
        val errorNoBuildsFound = mm("<gray>No builds found</gray>")

        fun buildCreated(name: String) = mm(
            "<green><aqua><name></aqua> created</green>",
            Placeholder.unparsed("name", name),
        )

        fun buildDeleted(name: String) = mm(
            "<green><aqua><name></aqua> deleted</green>",
            Placeholder.unparsed("name", name),
        )

        fun buildMoved(name: String) = mm(
            "<green><aqua><name></aqua> moved to your location</green>",
            Placeholder.unparsed("name", name),
        )

        val buildUpdated = mm("<green>Build updated</green>")

        fun buildVoteRecorded(name: String) = mm(
            "<green>You voted for <aqua><name></aqua></green>",
            Placeholder.unparsed("name", name),
        )

        fun buildVoteRemoved(name: String) = mm(
            "<gray>You removed your vote for <aqua><name></aqua></gray>",
            Placeholder.unparsed("name", name),
        )

        fun buildCreatedAnnouncement(
            playerName: String,
            buildName: String,
        ) = mm(
            "<gray>[<red>✎</red>] <aqua><player></aqua> created build \"<build>\"</gray>",
            Placeholder.unparsed("player", playerName),
            Placeholder.component("build", clickableBuildName(buildName)),
        )

        fun buildVoteAnnouncement(
            playerName: String,
            buildName: String,
        ) = mm(
            "<gray>[<red>❤</red>] <aqua><player></aqua> voted for build \"<build>\"</gray>",
            Placeholder.unparsed("player", playerName),
            Placeholder.component("build", clickableBuildName(buildName)),
        )

        val buildListTitle = "Build List"

        fun buildListItem(
            id: Int,
            name: String,
            votes: Int,
        ) = mm(
            "<gray>#<aqua><id></aqua> \"<aqua><name></aqua>\" (<aqua><votes></aqua> votes)</gray>",
            Placeholder.unparsed("id", id.toString()),
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("votes", votes.toString()),
        )

        val clickBuildFieldToEdit = Component.text("Click a field to edit:", NamedTextColor.GRAY)

        fun buildCreatedBy(owner: String) = mm(
            "<gray>Created by <aqua><owner></aqua></gray>",
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
            .text(name, NamedTextColor.AQUA)
            .decorate(TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand("/build $name"))
            .hoverEvent(HoverEvent.showText(Component.text("Click to teleport")))
    }

    inner class Homes {
        val homesCannotCreate = mm("<red>You cannot create a home</red>")

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
                            "<newline><gray><aqua><role></aqua>: <aqua>+<homes></aqua> homes</gray>",
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
            "<aqua><name></aqua> <gray>(<aqua><world></aqua>: <aqua><x></aqua>, <aqua><y></aqua>, <aqua><z></aqua>)</gray>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("world", world),
            Placeholder.unparsed("x", x.toString()),
            Placeholder.unparsed("y", y.toString()),
            Placeholder.unparsed("z", z.toString()),
        )

        val errorRenameHomeNotFound = mm("<red>Error: Home not found</red>")

        val renameHomeDialogTitle = Component.text("Rename Home")

        val homeIdInputLabel = Component.text("Home ID")

        val renameHomeButtonHover = Component.text("Click to rename the home")

        fun errorHomeNotFound(name: String) = mm(
            "<red>Could not find home <aqua><name></aqua></red>",
            Placeholder.unparsed("name", name),
        )

        fun homeCreated(name: String) = mm(
            "<green><aqua><name></aqua> created</green>",
            Placeholder.unparsed("name", name),
        )

        fun homeDeleted(name: String) = mm(
            "<green><aqua><name></aqua> deleted</green>",
            Placeholder.unparsed("name", name),
        )

        fun homeMoved(name: String) = mm(
            "<green><aqua><name></aqua> moved to your location</green>",
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
            "<gray>\"<aqua><u><name></u></aqua>\" (<aqua><world></aqua>)</gray>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("world", world),
        )

        val errorRenameWarpNotFound = mm("<red>Error: Warp not found</red>")

        val renameWarpDialogTitle = Component.text("Rename Warp")

        val warpIdInputLabel = Component.text("Warp ID")

        val renameWarpButtonHover = Component.text("Click to rename the warp")

        fun errorWarpNotFound(name: String) = mm(
            "<red>Could not find warp <aqua><name></aqua></red>",
            Placeholder.unparsed("name", name),
        )

        fun warpCreated(name: String) = mm(
            "<green><aqua><name></aqua> created</green>",
            Placeholder.unparsed("name", name),
        )

        fun warpDeleted(name: String) = mm(
            "<green><aqua><name></aqua> deleted</green>",
            Placeholder.unparsed("name", name),
        )

        fun warpMoved(name: String) = mm(
            "<green><aqua><name></aqua> moved to your location</green>",
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
            "<gray><aqua><name></aqua> has been banned</gray>",
            Placeholder.unparsed("name", name),
        )

        fun playerBanMessage(
            reason: Component?,
            expiresAt: String?,
        ) = mm(
            """
            <color:red><b>This account is banned.</b></color>

            <color:gray>Reason:</color> <reason>
            <color:gray>Expires:</color> <aqua><expires></aqua>

            <color:aqua>Appeal @ https://projectcitybuild.com</color>
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
            "<green><aqua><name></aqua> has been banned</green>",
            Placeholder.unparsed("name", name),
        )

        fun clickToEditBan(url: String): Component {
            val link = Component
                .text("here", NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(
                    HoverEvent.showText(
                        Component.text(url, NamedTextColor.AQUA).decorate(TextDecoration.UNDERLINED),
                    ),
                )
            return mm(
                "<gray>Click <link> to edit the ban or add additional information</gray>",
                Placeholder.component("link", link),
            )
        }
    }

    inner class Sync {
        val syncFetchingPlayerData = mm("<gray>Fetching player data...</gray>")

        fun syncFetchingPlayerDataFor(name: String) = mm(
            "<gray>Fetching player data for <aqua><name></aqua>...</gray>",
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
        fun opElevationAlreadyActive(remaining: String?) = mm(
            "<red>Error: You are already OP elevated (remaining: <aqua><remaining></aqua>)</red>",
            Placeholder.unparsed("remaining", remaining ?: "unknown"),
        )

        val opElevationNotActive = mm("<gray>You are not currently OP elevated</gray>")

        fun opElevationActive(remaining: String?) = mm(
            "<gray>You are OP elevated (remaining: <aqua><remaining></aqua>)</gray>",
            Placeholder.unparsed("remaining", remaining ?: "unknown"),
        )

        val errorOpElevationReasonRequired = mm("<red>Error: elevation reason is required</red>")

        fun rolesSet(roles: String) = mm(
            "<green>Your roles have been set to <aqua><roles></aqua></green><newline><gray>Use /sync or reconnect to revert this</gray>",
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

        val opElevationRevoked = mm("<green><i>OP elevation was revoked</i></green>")

        val opElevationExpired = mm("<gray><i>OP elevation expired</i></gray>")

        fun opElevationGranted(remainingTime: String) = mm(
            "<green>OP status granted (remaining: <aqua><remaining></aqua>)</green>",
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
                "<red>Please specify an email address to receive your registration code</red>" +
                    "<newline><gray>Example usage: <bold>/register your@email.com</bold></gray>",
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

        val nightVisionToggledOn = mm("<green><i>Night vision toggled on</i></green>")

        val nightVisionToggledOff = mm("<green><i>Night vision toggled off</i></green>")

        val receivedInvisFrame = mm("<green><i>You received an invisible item frame</i></green>")

        val receivedInvisFrameGlowing = mm("<green><i>You received an invisible glowing frame</i></green>")

        fun renamedItem(newName: TextComponent) = mm(
            "<gray>Renamed item in hand to <name></gray>",
            Placeholder.component("name", newName),
        )
    }

    inner class Gameplay {
        val burnCommandDescription = "Sets a player's fire duration"
        val extinguishCommandDescription = "Extinguishes a player"
        val feedCommandDescription = "Restores a player's hunger"
        val gameModeCommandDescription = "Changes a player's game mode"
        val healCommandDescription = "Restores a player's health"
        val iceCommandDescription = "Freezes a player for a duration"
        val killCommandDescription = "Kills a player"
        val purgeCommandDescription = "Removes a player's active potion effects"
        val dieCommandDescription = "Kills a player"
        val xpCommandDescription = "Changes a player's experience points"
        val timeCommandDescription = "Views or changes world time"
        val playerTimeCommandDescription = "Views or changes a player's personal time"
        val weatherCommandDescription = "Changes the weather in a world"
        val playerWeatherCommandDescription = "Views or changes a player's personal weather"

        fun playerSetOnFire(name: String) = mm(
            "<green>Set <aqua><name></aqua> on fire</green>",
            Placeholder.unparsed("name", name),
        )

        fun playerExtinguished(name: String) = mm(
            "<green>Extinguished <aqua><name></aqua></green>",
            Placeholder.unparsed("name", name),
        )

        fun playerNotOnFire(name: String) = mm(
            "<gray><aqua><name></aqua> is not on fire</gray>",
            Placeholder.unparsed("name", name),
        )

        fun playerFed(name: String) = mm(
            "<green>Fed <aqua><name></aqua></green>",
            Placeholder.unparsed("name", name),
        )

        fun playerGameModeChanged(
            name: String,
            gameMode: String,
        ) = mm(
            "<green>Changed <aqua><name></aqua>'s game mode to <aqua><game-mode></aqua></green>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("game-mode", gameMode),
        )

        fun playerHealed(name: String) = mm(
            "<green>Healed <aqua><name></aqua></green>",
            Placeholder.unparsed("name", name),
        )

        fun playerIced(
            name: String,
            seconds: Int,
        ) = mm(
            "<green>Iced <aqua><name></aqua> for <aqua><seconds></aqua> seconds</green>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("seconds", seconds.toString()),
        )

        fun playerKilled(name: String) = mm(
            "<green>Killed <aqua><name></aqua></green>",
            Placeholder.unparsed("name", name),
        )

        fun killedBy(name: String) = mm(
            "<gray>You were killed by <aqua><name></aqua></gray>",
            Placeholder.unparsed("name", name),
        )

        val noActivePotionEffects = mm("<gray>No active potion effects to purge</gray>")

        fun playerEffectsPurged(
            num: Int,
            name: String,
        ) = mm(
            "<green>Removed <aqua><num></aqua> active potion effects from <aqua><name></aqua></green>",
            Placeholder.unparsed("num", num.toString()),
            Placeholder.unparsed("name", name),
        )

        val playerDied = mm("<red>You died.</red>")

        fun playerExperienceSet(
            name: String,
            experience: Int,
        ) = mm(
            "<green>Set <aqua><name></aqua>'s experience points to <aqua><experience></aqua>.</green>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("experience", experience.toString()),
        )

        fun playerExperienceAdded(
            name: String,
            amount: Long,
            currentExperience: Int,
        ) = mm(
            "<green>Added <aqua><amount></aqua> experience points to <aqua><name></aqua>. " +
                "They now have <aqua><current-experience></aqua> experience points.</green>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("amount", amount.toString()),
            Placeholder.unparsed("current-experience", currentExperience.toString()),
        )

        fun playerExperienceSubtracted(
            name: String,
            amount: Long,
            currentExperience: Int,
        ) = mm(
            "<green>Subtracted <aqua><amount></aqua> experience points from <aqua><name></aqua>. " +
                "They now have <aqua><current-experience></aqua> experience points.</green>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("amount", amount.toString()),
            Placeholder.unparsed("current-experience", currentExperience.toString()),
        )

        fun playerExperienceReset(name: String) = mm(
            "<green>Reset <aqua><name></aqua>'s experience points to 0.</green>",
            Placeholder.unparsed("name", name),
        )

        fun worldTimeCurrent(
            world: String,
            time: String,
        ) = mm(
            "<gray>The time in <aqua><world></aqua> is <aqua><time></aqua></gray>",
            Placeholder.unparsed("world", world),
            Placeholder.unparsed("time", time),
        )

        fun worldTimeSet(
            world: String,
            time: String,
        ) = mm(
            "<green>Set the time in <aqua><world></aqua> to <aqua><time></aqua></green>",
            Placeholder.unparsed("world", world),
            Placeholder.unparsed("time", time),
        )

        val errorTimeWorldRequired = mm("<red>Specify a world when using this command from the console</red>")

        fun worldTimeAdded(
            world: String,
            time: String,
        ) = mm(
            "<green>Added <aqua><time></aqua> to the time in <aqua><world></aqua></green>",
            Placeholder.unparsed("time", time),
            Placeholder.unparsed("world", world),
        )

        fun playerTimeNormal(name: String) = mm(
            "<gray><aqua><name></aqua> uses the world's time</gray>",
            Placeholder.unparsed("name", name),
        )

        fun playerTimeCurrent(
            name: String,
            time: String,
            relative: Boolean,
        ) = mm(
            "<gray><aqua><name></aqua>'s personal time is <aqua><time></aqua> (<aqua><mode></aqua>)</gray>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("time", time),
            Placeholder.unparsed("mode", if (relative) "relative" else "fixed"),
        )

        fun playerTimeSet(
            name: String,
            time: String,
            relative: Boolean,
        ) = mm(
            "<green>Set <aqua><name></aqua>'s personal time to <aqua><time></aqua> (<aqua><mode></aqua>)</green>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("time", time),
            Placeholder.unparsed("mode", if (relative) "relative" else "fixed"),
        )

        fun playerTimeReset(name: String) = mm(
            "<green>Reset <aqua><name></aqua>'s personal time to the world's time</green>",
            Placeholder.unparsed("name", name),
        )

        fun weatherSet(
            world: String,
            weather: String,
            durationSeconds: Int?,
        ) = if (durationSeconds == null) {
            mm(
                "<green>Set the weather in <aqua><world></aqua> to <aqua><weather></aqua></green>",
                Placeholder.unparsed("world", world),
                Placeholder.unparsed("weather", weather),
            )
        } else {
            mm(
                "<green>Set the weather in <aqua><world></aqua> to <aqua><weather></aqua> " +
                    "for <aqua><duration></aqua> seconds</green>",
                Placeholder.unparsed("world", world),
                Placeholder.unparsed("weather", weather),
                Placeholder.unparsed("duration", durationSeconds.toString()),
            )
        }

        fun playerWeatherNormal(name: String) = mm(
            "<gray><aqua><name></aqua> uses the world's weather</gray>",
            Placeholder.unparsed("name", name),
        )

        fun playerWeatherCurrent(
            name: String,
            weather: String,
        ) = mm(
            "<gray><aqua><name></aqua>'s personal weather is <aqua><weather></aqua></gray>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("weather", weather),
        )

        fun playerWeatherSet(
            name: String,
            weather: String,
        ) = mm(
            "<green>Set <aqua><name></aqua>'s personal weather to <aqua><weather></aqua></green>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("weather", weather),
        )

        fun playerWeatherReset(name: String) = mm(
            "<green>Reset <aqua><name></aqua>'s personal weather to the world's weather</green>",
            Placeholder.unparsed("name", name),
        )

        fun errorInvalidTime(time: String) = mm(
            "<red>Invalid time: <aqua><time></aqua></red>",
            Placeholder.unparsed("time", time),
        )

        fun errorWorldNotFound(world: String) = mm(
            "<red>World <aqua><world></aqua> was not found</red>",
            Placeholder.unparsed("world", world),
        )

        val errorWeatherWorldRequired = mm("<red>Specify a world when using this command from the console</red>")

        fun errorPlayerMaxHealthUnavailable(name: String) = mm(
            "<red>Player <aqua><name></aqua> has no maximum-health attribute</red>",
            Placeholder.unparsed("name", name),
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
            "<yellow><aqua><name></aqua> wants to teleport to you. Use " +
                "<click:run_command:'/tp accept'><hover:show_text:'Click to accept'>" +
                "<aqua><underlined>/tp accept</underlined></aqua></hover></click> or " +
                "<click:run_command:'/tp deny'><hover:show_text:'Click to deny'>" +
                "<aqua><underlined>/tp deny</underlined></aqua></hover></click>.</yellow>",
            Placeholder.unparsed("name", name),
        )

        fun teleportHereRequestReceived(name: String) = mm(
            "<yellow><aqua><name></aqua> wants you to teleport to them. Use " +
                "<click:run_command:'/tp accept'><hover:show_text:'Click to accept'>" +
                "<aqua><underlined>/tp accept</underlined></aqua></hover></click> or " +
                "<click:run_command:'/tp deny'><hover:show_text:'Click to deny'>" +
                "<aqua><underlined>/tp deny</underlined></aqua></hover></click>.</yellow>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestAccepted(name: String) = mm(
            "<green>Teleport request from <aqua><name></aqua> accepted</green>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestDenied(name: String) = mm(
            "<red>Teleport request from <aqua><name></aqua> denied</red>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestWasDenied(name: String) = mm(
            "<red><aqua><name></aqua> denied your teleport request</red>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestCancelled(name: String) = mm(
            "<yellow>Teleport request to <aqua><name></aqua> cancelled</yellow>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestWasCancelled(name: String) = mm(
            "<yellow><aqua><name></aqua> cancelled their teleport request</yellow>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestExpired(name: String) = mm(
            "<yellow>Teleport request from <aqua><name></aqua> expired</yellow>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestSentExpired(name: String) = mm(
            "<yellow>Your teleport request to <aqua><name></aqua> expired</yellow>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestDisconnected(name: String) = mm(
            "<yellow>Teleport request involving <aqua><name></aqua> was removed because a player disconnected</yellow>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestCreated(
            name: String,
            ttl: Duration,
        ) = mm(
            "<green>Teleport request to <aqua><name></aqua> created (expires in <aqua><seconds></aqua> seconds)</green>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("seconds", ttl.inWholeSeconds.toString()),
        )

        fun teleportCompleted(name: String) = mm(
            "<green>Teleported to <aqua><name></aqua></green>",
            Placeholder.unparsed("name", name),
        )

        fun teleportOperationSucceeded(
            player: String,
            target: String,
        ) = mm(
            "<green>Teleported <aqua><player></aqua> to <aqua><target></aqua></green>",
            Placeholder.unparsed("player", player),
            Placeholder.unparsed("target", target),
        )

        fun teleportReceived(name: String) = mm(
            "<gray><aqua><name></aqua> teleported to you</gray>",
            Placeholder.unparsed("name", name),
        )

        fun teleportBackCompleted(name: String) = mm(
            "<green><aqua><name></aqua> returned to their previous location</green>",
            Placeholder.unparsed("name", name),
        )

        fun teleportBulkSummary(
            succeeded: Int,
            failed: Int,
        ) = mm(
            "<green>Teleported <aqua><succeeded></aqua> player(s)</green><gray>; <aqua><failed></aqua> failed</gray>",
            Placeholder.unparsed("succeeded", succeeded.toString()),
            Placeholder.unparsed("failed", failed.toString()),
        )

        fun teleportRequestsCreated(count: Int) = mm(
            "<green>Teleport requests sent to <aqua><count></aqua> player(s)</green>",
            Placeholder.unparsed("count", count.toString()),
        )

        fun teleportedToCoordinate(
            x: Double,
            y: Double,
            z: Double,
        ) = mm(
            "<gray><i>⚡ Teleported to <b>{<aqua><x></aqua>, <aqua><y></aqua>, <aqua><z></aqua>}</b></i></gray>",
            Placeholder.unparsed("x", x.toString()),
            Placeholder.unparsed("y", y.toString()),
            Placeholder.unparsed("z", z.toString()),
        )

        fun teleportedToName(name: String) = mm(
            "<gray><i>⚡ Teleported to <b><aqua><name></aqua></b></i></gray>",
            Placeholder.unparsed("name", name),
        )

        fun teleportedToCoordinate(
            x: Int,
            y: Int,
            z: Int,
        ) = mm(
            "<gray><i>⚡ Teleported to <b>{<aqua><x></aqua>, <aqua><y></aqua>, <aqua><z></aqua>}</b></i></gray>",
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
                "{<aqua><x></aqua>, <aqua><y></aqua>, <aqua><z></aqua>} pitch=<aqua><pitch></aqua> yaw=<aqua><yaw></aqua>" +
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
            "<i><gray><aqua><name></aqua> was kicked: <aqua><reason></aqua></gray></i>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("reason", reason),
        )

        fun playerWasKicked(name: String) = mm(
            "<i><gray><aqua><name></aqua> was kicked</gray></i>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Connection {
        val errorUnableToAuthConnection =
            Component.text("Unable to authorize your connection. Please try again later.", NamedTextColor.RED)

        val playerStateUnavailableWarning =
            Component.text(
                "Warning: Unable to retrieve player data. Your permissions, rank, and other data may be missing. " +
                    "Please type /sync or try reconnecting in a moment.",
                NamedTextColor.YELLOW,
            )
    }

    inner class ServerList {
        val defaultServerListMotd = Component.text("Project City Build")
    }

    inner class Tablist {
        fun playerPing(ping: Int) = mm(
            " <gray><aqua><ping></aqua></gray>",
            Placeholder.unparsed("ping", ping.toString()),
        )
    }

    inner class Maintenance {
        val maintenanceConnectionDenied =
            mm("<bold>Server Maintenance</bold><newline><newline><gray>Please try again later</gray>")

        val maintenanceMotd = mm("<yellow>Server maintenance - be right back!</yellow>")

        val maintenanceReminder = mm("<italic><yellow>[Reminder] Maintenance mode is currently ON</yellow></italic>")

        fun maintenanceAlreadyInState(enabled: Boolean) = mm(
            "<red>Maintenance mode is already <aqua><state></aqua></red>",
            Placeholder.unparsed("state", enabled.onOff()),
        )

        fun maintenanceModeChanged(enabled: Boolean) = mm(
            "<green>Maintenance mode is now <aqua><state></aqua></green>",
            Placeholder.unparsed("state", enabled.onOff()),
        )

        fun maintenanceStatus(enabled: Boolean) = mm(
            "<gray>Maintenance mode is currently <aqua><state></aqua></gray>",
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
        val borderReached = mm("<red>You have reached the world border.</red>")

        fun borderStatus(
            shape: String,
            centerX: Double,
            centerZ: Double,
            radius: Double,
        ) = mm(
            "<gray>World border: <aqua><shape></aqua>, center <aqua><x></aqua>, <aqua><z></aqua>; " +
                "radius <aqua><radius></aqua> blocks.</gray>",
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
            "<gray>World border: rectangle, center <aqua><x></aqua>, <aqua><z></aqua>; " +
                "width <aqua><width></aqua>, depth <aqua><depth></aqua> blocks.</gray>",
            Placeholder.unparsed("x", centerX.toString()),
            Placeholder.unparsed("z", centerZ.toString()),
            Placeholder.unparsed("width", width.toString()),
            Placeholder.unparsed("depth", depth.toString()),
        )

        fun invalidBorder(reason: String?) = mm(
            "<red>Invalid world border: <aqua><reason></aqua></red>",
            Placeholder.unparsed("reason", reason ?: "unknown reason"),
        )

        fun borderSet(
            shape: String,
            centerX: Double,
            centerZ: Double,
            radius: Double,
        ) = mm(
            "<green>World border set: <aqua><shape></aqua>, center <aqua><x></aqua>, <aqua><z></aqua>; " +
                "radius <aqua><radius></aqua> blocks.</green>",
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
            "<green>World border set: rectangle, center <aqua><x></aqua>, <aqua><z></aqua>; " +
                "width <aqua><width></aqua>, depth <aqua><depth></aqua> blocks.</green>",
            Placeholder.unparsed("x", centerX.toString()),
            Placeholder.unparsed("z", centerZ.toString()),
            Placeholder.unparsed("width", width.toString()),
            Placeholder.unparsed("depth", depth.toString()),
        )

        fun borderRelocationFailures(count: Int) = mm(
            "<yellow>World border was committed, but relocation failed for <aqua><count></aqua> player(s). See server logs.</yellow>",
            Placeholder.unparsed("count", count.toString()),
        )

        val borderRespawnRelocationFailed =
            mm("<red>Could not safely relocate your respawn inside the world border. Please contact staff.</red>")

        val fillRequiresBorder = mm("<red>Set a world border before filling chunks.</red>")

        fun fillLargeWarning(chunkCountUpperBound: Long) = mm(
            "<yellow>This is a large fill (up to <aqua><count></aqua> chunks) and may take a long time. " +
                "Click <click:run_command:'/worlds border fill confirm'>" +
                "<hover:show_text:'Click to confirm the fill'><aqua><underlined>" +
                "/worlds border fill confirm</underlined></aqua></hover></click> to continue.</yellow>",
            Placeholder.unparsed("count", chunkCountUpperBound.toString()),
        )

        fun fillPreview(
            chunkCountUpperBound: Long,
            worldName: String,
        ) = mm(
            "<yellow>Fill preview: up to <aqua><count></aqua> chunks may be checked in <aqua><world></aqua>. " +
                "Only missing chunks will be generated. Click " +
                "<click:run_command:'/worlds border fill confirm'>" +
                "<hover:show_text:'Click to confirm the fill'><aqua><underlined>" +
                "/worlds border fill confirm</underlined></aqua></hover></click> within 60 seconds to continue.</yellow>",
            Placeholder.unparsed("count", chunkCountUpperBound.toString()),
            Placeholder.unparsed("world", worldName),
        )

        val fillPreviewMissing = mm("<red>There is no active fill preview. Run /worlds border fill first.</red>")

        val fillWrongWorld = mm("<red>You must confirm the fill in the world where it was previewed.</red>")

        val fillBorderChanged = mm(
            "<red>The world border changed. Run /worlds border fill again to preview the updated area.</red>",
        )

        val fillAlreadyRunning = mm("<red>A world border fill is already running for this world.</red>")

        val fillStarted = mm("<yellow>Starting world border fill...</yellow>")

        val fillStoppedBorderChanged = mm("<red>World border changed during fill; the operation was stopped.</red>")

        fun fillProgress(
            checked: Long,
            chunkCountUpperBound: Long,
            generated: Long,
        ) = mm(
            "<yellow>World border fill progress: checked <aqua><checked></aqua> of up to <aqua><total></aqua> chunks; " +
                "generated <aqua><generated></aqua>.</yellow>",
            Placeholder.unparsed("checked", checked.toString()),
            Placeholder.unparsed("total", chunkCountUpperBound.toString()),
            Placeholder.unparsed("generated", generated.toString()),
        )

        fun fillComplete(
            checked: Long,
            chunkCountUpperBound: Long,
            generated: Long,
        ) = mm(
            "<green>World border fill complete: checked <aqua><checked></aqua> chunks " +
                "(up to <aqua><total></aqua> in the border); generated <aqua><generated></aqua>.</green>",
            Placeholder.unparsed("checked", checked.toString()),
            Placeholder.unparsed("total", chunkCountUpperBound.toString()),
            Placeholder.unparsed("generated", generated.toString()),
        )

        fun fillStatus(
            checked: Long,
            chunkCountUpperBound: Long,
            generated: Long,
            status: String,
        ) = mm(
            "<yellow>World border fill <aqua><status></aqua>: checked <aqua><checked></aqua> of up to " +
                "<aqua><total></aqua> chunks; generated <aqua><generated></aqua>.</yellow>",
            Placeholder.unparsed("status", status),
            Placeholder.unparsed("checked", checked.toString()),
            Placeholder.unparsed("total", chunkCountUpperBound.toString()),
            Placeholder.unparsed("generated", generated.toString()),
        )

        fun fillCancelled(
            checked: Long,
            chunkCountUpperBound: Long,
            generated: Long,
        ) = mm(
            "<yellow>World border fill cancelled: checked <aqua><checked></aqua> of up to " +
                "<aqua><total></aqua> chunks; generated <aqua><generated></aqua>.</yellow>",
            Placeholder.unparsed("checked", checked.toString()),
            Placeholder.unparsed("total", chunkCountUpperBound.toString()),
            Placeholder.unparsed("generated", generated.toString()),
        )

        val fillFailed = mm("<red>World border fill failed. See the server log for details.</red>")
        val fillNoActiveJob = mm("<red>There is no world border fill running in this world.</red>")
        val fillCancelRequested = mm("<yellow>World border fill cancellation requested.</yellow>")
    }
}
