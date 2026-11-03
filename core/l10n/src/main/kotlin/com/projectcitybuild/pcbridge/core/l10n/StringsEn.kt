package com.projectcitybuild.pcbridge.core.l10n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Location
import kotlin.time.Duration

private object Hex {
    const val ERROR = "#FF6B6B"
    const val SUCCESS = "#6BFF95"
    const val WARNING = "#FFD166"
    const val SECONDARY = "#B0B8C4"
    const val DYNAMIC = "#64D8FF"
    const val LINK = "#A78BFA"

    val ERROR_COLOR = color(ERROR)
    val WARNING_COLOR = color(WARNING)
    val SECONDARY_COLOR = color(SECONDARY)
    val DYNAMIC_COLOR = color(DYNAMIC)
    val LINK_COLOR = color(LINK)

    private fun color(hex: String) = TextColor.color(hex.removePrefix("#").toInt(16))
}

val l10n = StringsEn()

class StringsEn {
    private val miniMessage = MiniMessage.miniMessage()
    private val errorColor = Hex.ERROR_COLOR
    private val warningColor = Hex.WARNING_COLOR
    private val secondaryColor = Hex.SECONDARY_COLOR
    private val dynamicColor = Hex.DYNAMIC_COLOR
    private val linkColor = Hex.LINK_COLOR

    private fun mm(
        template: String,
        vararg resolvers: TagResolver,
    ): Component = miniMessage.deserialize(template, *resolvers)

    private fun Boolean.onOff(): String = if (this) "ON" else "OFF"

    private fun requiredField(label: String) = Component.text(label).append(Component.text("*", errorColor))

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
    val portals = Portals()
    val inventories = Inventories()

    inner class Pagination {
        val pagePrevButton = mm("<${Hex.LINK}>[← Prev]</${Hex.LINK}>")

        val pageNextButton = mm("<${Hex.LINK}>[Next →]</${Hex.LINK}>")

        val pagePrevButtonHover = Component.text("Click for previous page")

        val pageNextButtonHover = Component.text("Click for next page")

        val pageLabel = "Page"

        val errorPageNotFound = mm("<${Hex.ERROR}>Page not found</${Hex.ERROR}>")
    }

    inner class Errors {
        val errorOnlyPlayersCanUseThisCommand = mm("<${Hex.ERROR}>Only players can use this command</${Hex.ERROR}>")

        val errorNewNameCannotBeEmpty = mm("<${Hex.ERROR}>Error: New name cannot be empty</${Hex.ERROR}>")

        fun errorWithMessage(message: String?) = mm(
            "<${Hex.ERROR}>Error: <${Hex.DYNAMIC}><message></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("message", message ?: "Unknown error"),
        )

        fun invalidArgumentValue(value: String) = mm(
            "<${Hex.ERROR}>Invalid value: <${Hex.DYNAMIC}><value></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("value", value),
        )

        fun errorPlayerNotFound(name: String) = mm(
            "<${Hex.ERROR}>Player <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> not found</${Hex.ERROR}>",
            Placeholder.unparsed("name", name),
        )

        fun commandError(message: String?) = mm(
            "<${Hex.ERROR}><${Hex.DYNAMIC}><message></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("message", message ?: "Unknown error"),
        )

        fun commandErrorWithPrefix(message: String) = mm(
            "<${Hex.ERROR}>Error: <${Hex.DYNAMIC}><message></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("message", message),
        )

        fun commandErrorWithPrefix(message: Component) = mm(
            "<${Hex.ERROR}>Error: <message></${Hex.ERROR}>",
            Placeholder.component("message", message),
        )

        val commandErrorValidationFailed = commandErrorWithPrefix("Validation failed")

        val commandErrorNotFound = commandErrorWithPrefix("Not Found")

        val commandErrorNotPermitted = commandErrorWithPrefix("Not permitted to perform this action")

        val commandErrorConflict = commandErrorWithPrefix("Conflict")

        fun commandErrorCooldown(seconds: Long) = mm(
            "<${Hex.ERROR}>Error: Please wait <${Hex.DYNAMIC}><seconds></${Hex.DYNAMIC}> seconds before trying again</${Hex.ERROR}>",
            Placeholder.unparsed("seconds", seconds.toString()),
        )

        val commandUnexpectedError = commandError("An unexpected error occurred")

        fun errorWorldNotFound(name: String) = mm(
            "<${Hex.ERROR}>Could not find world <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Lists {
        fun teleportTo(name: String) = mm(
            "Teleport to <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Builds {
        val errorNoBuildsFound = mm("<${Hex.SECONDARY}>No builds found</${Hex.SECONDARY}>")

        fun buildCreated(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> created</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun buildDeleted(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> deleted</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun buildMoved(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> moved to your location</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        val buildUpdated = mm("<${Hex.SUCCESS}>Build updated</${Hex.SUCCESS}>")

        fun buildVoteRecorded(name: String) = mm(
            "<${Hex.SUCCESS}>You voted for <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun buildVoteRemoved(name: String) = mm(
            "<${Hex.SECONDARY}>You removed your vote for <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        fun buildCreatedAnnouncement(
            playerName: String,
            buildName: String,
        ) = mm(
            "<${Hex.SECONDARY}>[<${Hex.LINK}>✎</${Hex.LINK}>] " +
                "<${Hex.DYNAMIC}><player></${Hex.DYNAMIC}> created build \"<build>\"</${Hex.SECONDARY}>",
            Placeholder.unparsed("player", playerName),
            Placeholder.component("build", clickableBuildName(buildName)),
        )

        fun buildVoteAnnouncement(
            playerName: String,
            buildName: String,
        ) = mm(
            "<${Hex.SECONDARY}>[<${Hex.LINK}>❤</${Hex.LINK}>] " +
                "<${Hex.DYNAMIC}><player></${Hex.DYNAMIC}> voted for build \"<build>\"</${Hex.SECONDARY}>",
            Placeholder.unparsed("player", playerName),
            Placeholder.component("build", clickableBuildName(buildName)),
        )

        val buildListTitle = "Build List"

        fun buildListItem(
            id: Int,
            name: String,
            votes: Int,
        ) = mm(
            "<${Hex.SECONDARY}>#<${Hex.DYNAMIC}><id></${Hex.DYNAMIC}> " +
                "\"<${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>\" (<${Hex.DYNAMIC}><votes></${Hex.DYNAMIC}> " +
                "votes)</${Hex.SECONDARY}>",
            Placeholder.unparsed("id", id.toString()),
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("votes", votes.toString()),
        )

        val clickBuildFieldToEdit = Component.text("Click a field to edit:", secondaryColor)

        fun buildCreatedBy(owner: String) = mm(
            "<${Hex.SECONDARY}>Created by <${Hex.DYNAMIC}><owner></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("owner", owner),
        )

        fun buildTeleportedTo(name: String) = mm(
            "<${Hex.SECONDARY}>Teleported to <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        fun buildDescription(description: Component) = mm(
            "<${Hex.SECONDARY}>---<newline><description></${Hex.SECONDARY}>",
            Placeholder.component("description", description),
        )

        fun buildLore(lore: Component) = mm(
            "<${Hex.SECONDARY}>---<newline><italic><lore></italic></${Hex.SECONDARY}>",
            Placeholder.component("lore", lore),
        )

        private fun clickableBuildName(name: String) = Component
            .text(name, dynamicColor)
            .decorate(TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand("/build $name"))
            .hoverEvent(HoverEvent.showText(Component.text("Click to teleport")))
    }

    inner class Homes {
        val homesCannotCreate = mm("<${Hex.ERROR}>You cannot create a home</${Hex.ERROR}>")

        fun homesLimit(
            current: Int,
            max: Int,
        ) = mm(
            "You currently have <${Hex.DYNAMIC}><current></${Hex.DYNAMIC}> of <${Hex.DYNAMIC}><max></${Hex.DYNAMIC}> homes",
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
                            "<newline><${Hex.SECONDARY}><${Hex.DYNAMIC}><role></${Hex.DYNAMIC}>: " +
                                "<${Hex.DYNAMIC}>+<homes></${Hex.DYNAMIC}> homes</${Hex.SECONDARY}>",
                            Placeholder.unparsed("role", role),
                            Placeholder.unparsed("homes", homes.toString()),
                        ),
                    )
            }
            return mm(
                "<${Hex.SECONDARY}>The following roles grant you homes:</${Hex.SECONDARY}> <roles>",
                Placeholder.component("roles", roleLines),
            )
        }

        val homesLimitDivider = mm("<${Hex.SECONDARY}>---</${Hex.SECONDARY}>")

        val homesListTitle = "Your Homes"

        fun homesListItem(
            name: String,
            world: String,
            x: Int,
            y: Int,
            z: Int,
        ) = mm(
            "<${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> <${Hex.SECONDARY}>(<${Hex.DYNAMIC}><world></${Hex.DYNAMIC}>: " +
                "<${Hex.DYNAMIC}><x></${Hex.DYNAMIC}>, <${Hex.DYNAMIC}><y></${Hex.DYNAMIC}>, " +
                "<${Hex.DYNAMIC}><z></${Hex.DYNAMIC}>)</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("world", world),
            Placeholder.unparsed("x", x.toString()),
            Placeholder.unparsed("y", y.toString()),
            Placeholder.unparsed("z", z.toString()),
        )

        val errorRenameHomeNotFound = mm("<${Hex.ERROR}>Error: Home not found</${Hex.ERROR}>")

        val renameHomeDialogTitle = Component.text("Rename Home")

        val homeIdInputLabel = Component.text("Home ID")

        val renameHomeButtonHover = Component.text("Click to rename the home")

        fun errorHomeNotFound(name: String) = mm(
            "<${Hex.ERROR}>Could not find home <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("name", name),
        )

        fun homeCreated(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> created</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun homeDeleted(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> deleted</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun homeMoved(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> moved to your location</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun homeRenamed(newName: String) = mm(
            "<${Hex.SUCCESS}>Home renamed to <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", newName),
        )

        val noHomesFound = mm("<${Hex.SECONDARY}>No homes found</${Hex.SECONDARY}>")
    }

    inner class Warps {
        val warpsListTitle = "Warps"

        fun warpListItem(
            name: String,
            world: String,
        ) = mm(
            "<${Hex.SECONDARY}>\"<${Hex.DYNAMIC}><u><name></u></${Hex.DYNAMIC}>\" " +
                "(<${Hex.DYNAMIC}><world></${Hex.DYNAMIC}>)</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("world", world),
        )

        val errorRenameWarpNotFound = mm("<${Hex.ERROR}>Error: Warp not found</${Hex.ERROR}>")

        val renameWarpDialogTitle = Component.text("Rename Warp")

        val warpIdInputLabel = Component.text("Warp ID")

        val renameWarpButtonHover = Component.text("Click to rename the warp")

        fun errorWarpNotFound(name: String) = mm(
            "<${Hex.ERROR}>Could not find warp <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("name", name),
        )

        fun warpCreated(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> created</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun warpDeleted(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> deleted</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun warpMoved(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> moved to your location</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun warpRenamed(newName: String) = mm(
            "<${Hex.SUCCESS}>Warp renamed to <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", newName),
        )

        val noWarpsFound = mm("<${Hex.SECONDARY}>No warps found</${Hex.SECONDARY}>")
    }

    inner class Bans {
        fun playerBannedAnnouncement(name: String) = mm(
            "<${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> has been banned</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        fun playerBanMessage(
            reason: Component?,
            expiresAt: String?,
        ) = mm(
            """
            <${Hex.ERROR}><b>This account is banned.</b></${Hex.ERROR}>

            <${Hex.SECONDARY}>Reason:</${Hex.SECONDARY}> <reason>
            <${Hex.SECONDARY}>Expires:</${Hex.SECONDARY}> <${Hex.DYNAMIC}><expires></${Hex.DYNAMIC}>

            <${Hex.LINK}>Appeal @ https://projectcitybuild.com</${Hex.LINK}>
            """.trimIndent(),
            Placeholder.component("reason", reason ?: Component.text("No reason provided")),
            Placeholder.unparsed("expires", expiresAt ?: "Never"),
        )

        fun ipBanMessage(reason: Component?) = mm(
            """
            <${Hex.ERROR}><b>This IP address is banned.</b></${Hex.ERROR}>

            <${Hex.SECONDARY}>Reason:</${Hex.SECONDARY}> <reason>

            <${Hex.LINK}>Appeal @ https://projectcitybuild.com</${Hex.LINK}>
            """.trimIndent(),
            Placeholder.component("reason", reason ?: Component.text("No reason provided")),
        )

        val createBanDialogTitle = Component.text("Ban Player")

        val defaultBanReason = "Griefing"

        val banPlayerNameInputLabel = requiredField("Player Name")

        val banReasonInputLabel = requiredField("Reason (shown to player)")

        val banAdditionalInfoInputLabel = Component.text("Additional Info/Context")

        val confirmBanButton = Component.text("Ban Player", errorColor)

        val confirmBanButtonHover = Component.text("Click to ban the player")

        fun playerHasBeenBanned(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> has been banned</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun clickToEditBan(url: String): Component {
            val link = Component
                .text("here", linkColor)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(
                    HoverEvent.showText(
                        Component.text(url, dynamicColor).decorate(TextDecoration.UNDERLINED),
                    ),
                )
            return mm(
                "<${Hex.SECONDARY}>Click <link> to edit the ban or add additional information</${Hex.SECONDARY}>",
                Placeholder.component("link", link),
            )
        }
    }

    inner class Sync {
        val syncFetchingPlayerData = mm("<${Hex.SECONDARY}>Fetching player data...</${Hex.SECONDARY}>")

        fun syncFetchingPlayerDataFor(name: String) = mm(
            "<${Hex.SECONDARY}>Fetching player data for <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>...</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        val syncPlayerDataSynced = mm("<${Hex.SUCCESS}>Player data synced</${Hex.SUCCESS}>")

        val syncAccountSynced = mm("<${Hex.SUCCESS}>Your account has been synced</${Hex.SUCCESS}>")
    }

    inner class Config {
        val configFetching = mm("<${Hex.SECONDARY}>Fetching config...</${Hex.SECONDARY}>")

        val configReloaded = mm("<${Hex.SUCCESS}>Remote config reloaded</${Hex.SUCCESS}>")
    }

    inner class Warnings {
        val warningCreationInstructions =
            mm(
                "<${Hex.SECONDARY}>Click the link below to create a warning for this player</${Hex.SECONDARY}>",
            )

        fun warningCreationLink(url: String) = Component
            .text(url, dynamicColor)
            .decorate(TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.openUrl(url))
    }

    inner class Pim {
        fun opElevationAlreadyActive(remaining: String?) = mm(
            "<${Hex.ERROR}>Error: You are already OP elevated (remaining: <${Hex.DYNAMIC}><remaining></${Hex.DYNAMIC}>)</${Hex.ERROR}>",
            Placeholder.unparsed("remaining", remaining ?: "unknown"),
        )

        val opElevationNotActive = mm("<${Hex.SECONDARY}>You are not currently OP elevated</${Hex.SECONDARY}>")

        fun opElevationActive(remaining: String?) = mm(
            "<${Hex.SECONDARY}>You are OP elevated (remaining: <${Hex.DYNAMIC}><remaining></${Hex.DYNAMIC}>)</${Hex.SECONDARY}>",
            Placeholder.unparsed("remaining", remaining ?: "unknown"),
        )

        val errorOpElevationReasonRequired = mm("<${Hex.ERROR}>Error: elevation reason is required</${Hex.ERROR}>")

        fun rolesSet(roles: String) = mm(
            "<${Hex.SUCCESS}>Your roles have been set to <${Hex.DYNAMIC}><roles></${Hex.DYNAMIC}></${Hex.SUCCESS}>" +
                "<newline><${Hex.SECONDARY}>Use /sync or reconnect to revert this</${Hex.SECONDARY}>",
            Placeholder.unparsed("roles", roles),
        )

        val opElevationDialogTitle = Component.text("Warning")

        val opElevationWarning = mm(
            """
            #<${Hex.WARNING}><b>You are about to receive temporary Operator (OP) privileges.</b></${Hex.WARNING}>
            #
            #• <${Hex.LINK}>Full administrative permissions</${Hex.LINK}> will be granted.
            #• Your OP status will be <${Hex.LINK}>automatically revoked</${Hex.LINK}> after the set duration.
            #• You may revoke it early at any time with: <${Hex.LINK}><b>/pim op revoke</b></${Hex.LINK}>
            #
            #<${Hex.WARNING}><b>⚠ IMPORTANT</b></${Hex.WARNING}>
            #<i>
            #OP privileges bypass the normal permission system. They may override protections or cause unintended behavior.
            #
            #Please revoke your OP status as soon as your task is complete.
            #</i>
            """.trimMargin("#"),
        )

        val opElevationReasonInputLabel = requiredField("Reason for Elevation")

        val opElevationConfirmButton = Component.text("I understand.")

        val opElevationRevoked = mm("<${Hex.SUCCESS}><i>OP elevation was revoked</i></${Hex.SUCCESS}>")

        val opElevationExpired = mm("<${Hex.SECONDARY}><i>OP elevation expired</i></${Hex.SECONDARY}>")

        fun opElevationGranted(remainingTime: String) = mm(
            "<${Hex.SUCCESS}>OP status granted (remaining: <${Hex.DYNAMIC}><remaining></${Hex.DYNAMIC}>)</${Hex.SUCCESS}>",
            Placeholder.unparsed("remaining", remainingTime),
        )
    }

    inner class ChatBadge {
        val chatBadgeHeader = Component.text("Badges", linkColor)

        val chatBadgeDivider = Component.text("---", secondaryColor)
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
            "Current name: <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>",
            Placeholder.unparsed("name", name),
        )

        val newNameInputLabel = requiredField("New Name")

        val dialogSaveButton = Component.text("Save")

        val dialogCancelButton = Component.text("Cancel")

        val dialogCancelButtonGray = Component.text("Cancel", secondaryColor)

        val discardDialogInputHover = Component.text("Click to discard your input")
    }

    inner class Registration {
        val verifyRegistrationDialogTitle = Component.text("Enter Code")

        val registrationCodeInputLabel = requiredField("Code")

        val verifyRegistrationButton = Component.text("Verify")

        val errorNoRegisterEmailSpecified =
            mm(
                "<${Hex.ERROR}>Please specify an email address to receive your registration code</${Hex.ERROR}>" +
                    "<newline><${Hex.SECONDARY}>Example usage: <bold>/register your@email.com</bold></${Hex.SECONDARY}>",
            )

        val errorNoCodeSpecified = mm("<${Hex.ERROR}>You did not specify a code</${Hex.ERROR}>")

        val errorCodeInvalidOrExpired = mm("<${Hex.ERROR}>Code is invalid or expired</${Hex.ERROR}>")

        fun codeHasBeenEmailedTo(email: String) = mm(
            "A code has been emailed to <${Hex.DYNAMIC}><email></${Hex.DYNAMIC}>.<newline>Please type " +
                "the code below to finish registration.",
            Placeholder.unparsed("email", email),
        )

        val codeHasBeenEmailed = mm("Please type in the code emailed to you to finish registration.")

        val registrationComplete = mm("<${Hex.SUCCESS}>Registration complete! Your account will be synced momentarily...</${Hex.SUCCESS}>")
    }

    inner class Building {
        val errorNoItemToRename = mm("<${Hex.ERROR}>No item in hand to rename</${Hex.ERROR}>")

        val nightVisionToggledOn = mm("<${Hex.SUCCESS}><i>Night vision toggled on</i></${Hex.SUCCESS}>")

        val nightVisionToggledOff = mm("<${Hex.SUCCESS}><i>Night vision toggled off</i></${Hex.SUCCESS}>")

        val receivedInvisFrame = mm("<${Hex.SUCCESS}><i>You received an invisible item frame</i></${Hex.SUCCESS}>")

        val receivedInvisFrameGlowing = mm("<${Hex.SUCCESS}><i>You received an invisible glowing frame</i></${Hex.SUCCESS}>")

        fun renamedItem(newName: TextComponent) = mm(
            "<${Hex.SECONDARY}>Renamed item in hand to <name></${Hex.SECONDARY}>",
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
            "<${Hex.SUCCESS}>Set <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> on fire</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun playerExtinguished(name: String) = mm(
            "<${Hex.SUCCESS}>Extinguished <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun playerNotOnFire(name: String) = mm(
            "<${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> is not on fire</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        fun playerFed(name: String) = mm(
            "<${Hex.SUCCESS}>Fed <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun playerGameModeChanged(
            name: String,
            gameMode: String,
        ) = mm(
            "<${Hex.SUCCESS}>Changed <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s game mode to " +
                "<${Hex.DYNAMIC}><game-mode></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("game-mode", gameMode),
        )

        fun playerHealed(name: String) = mm(
            "<${Hex.SUCCESS}>Healed <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun playerIced(
            name: String,
            seconds: Int,
        ) = mm(
            "<${Hex.SUCCESS}>Iced <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> for " +
                "<${Hex.DYNAMIC}><seconds></${Hex.DYNAMIC}> seconds</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("seconds", seconds.toString()),
        )

        fun playerKilled(name: String) = mm(
            "<${Hex.SUCCESS}>Killed <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun killedBy(name: String) = mm(
            "<${Hex.SECONDARY}>You were killed by <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        val noActivePotionEffects = mm("<${Hex.SECONDARY}>No active potion effects to purge</${Hex.SECONDARY}>")

        fun playerEffectsPurged(
            num: Int,
            name: String,
        ) = mm(
            "<${Hex.SUCCESS}>Removed <${Hex.DYNAMIC}><num></${Hex.DYNAMIC}> active potion effects " +
                "from <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("num", num.toString()),
            Placeholder.unparsed("name", name),
        )

        val playerDied = mm("<${Hex.ERROR}>You died.</${Hex.ERROR}>")

        fun playerExperienceSet(
            name: String,
            experience: Int,
        ) = mm(
            "<${Hex.SUCCESS}>Set <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s experience points to " +
                "<${Hex.DYNAMIC}><experience></${Hex.DYNAMIC}>.</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("experience", experience.toString()),
        )

        fun playerExperienceAdded(
            name: String,
            amount: Long,
            currentExperience: Int,
        ) = mm(
            "<${Hex.SUCCESS}>Added <${Hex.DYNAMIC}><amount></${Hex.DYNAMIC}> experience points to " +
                "<${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>. " +
                "They now have <${Hex.DYNAMIC}><current-experience></${Hex.DYNAMIC}> experience points.</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("amount", amount.toString()),
            Placeholder.unparsed("current-experience", currentExperience.toString()),
        )

        fun playerExperienceSubtracted(
            name: String,
            amount: Long,
            currentExperience: Int,
        ) = mm(
            "<${Hex.SUCCESS}>Subtracted <${Hex.DYNAMIC}><amount></${Hex.DYNAMIC}> experience points " +
                "from <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>. " +
                "They now have <${Hex.DYNAMIC}><current-experience></${Hex.DYNAMIC}> experience points.</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("amount", amount.toString()),
            Placeholder.unparsed("current-experience", currentExperience.toString()),
        )

        fun playerExperienceReset(name: String) = mm(
            "<${Hex.SUCCESS}>Reset <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s experience points to 0.</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun worldTimeCurrent(
            world: String,
            time: String,
        ) = mm(
            "<${Hex.SECONDARY}>The time in <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}> is " +
                "<${Hex.DYNAMIC}><time></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("world", world),
            Placeholder.unparsed("time", time),
        )

        fun worldTimeSet(
            world: String,
            time: String,
        ) = mm(
            "<${Hex.SUCCESS}>Set the time in <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}> to " +
                "<${Hex.DYNAMIC}><time></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("world", world),
            Placeholder.unparsed("time", time),
        )

        val errorTimeWorldRequired = mm("<${Hex.ERROR}>Specify a world when using this command from the console</${Hex.ERROR}>")

        fun worldTimeAdded(
            world: String,
            time: String,
        ) = mm(
            "<${Hex.SUCCESS}>Added <${Hex.DYNAMIC}><time></${Hex.DYNAMIC}> to the time in " +
                "<${Hex.DYNAMIC}><world></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("time", time),
            Placeholder.unparsed("world", world),
        )

        fun playerTimeNormal(name: String) = mm(
            "<${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> uses the world's time</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        fun playerTimeCurrent(
            name: String,
            time: String,
            relative: Boolean,
        ) = mm(
            "<${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s personal time is " +
                "<${Hex.DYNAMIC}><time></${Hex.DYNAMIC}> " +
                "(<${Hex.DYNAMIC}><mode></${Hex.DYNAMIC}>)</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("time", time),
            Placeholder.unparsed("mode", if (relative) "relative" else "fixed"),
        )

        fun playerTimeSet(
            name: String,
            time: String,
            relative: Boolean,
        ) = mm(
            "<${Hex.SUCCESS}>Set <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s personal time to " +
                "<${Hex.DYNAMIC}><time></${Hex.DYNAMIC}> " +
                "(<${Hex.DYNAMIC}><mode></${Hex.DYNAMIC}>)</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("time", time),
            Placeholder.unparsed("mode", if (relative) "relative" else "fixed"),
        )

        fun playerTimeReset(name: String) = mm(
            "<${Hex.SUCCESS}>Reset <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s personal time to the world's time</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun weatherSet(
            world: String,
            weather: String,
            durationSeconds: Int?,
        ) = if (durationSeconds == null) {
            mm(
                "<${Hex.SUCCESS}>Set the weather in <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}> to " +
                    "<${Hex.DYNAMIC}><weather></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
                Placeholder.unparsed("world", world),
                Placeholder.unparsed("weather", weather),
            )
        } else {
            mm(
                "<${Hex.SUCCESS}>Set the weather in <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}> to " +
                    "<${Hex.DYNAMIC}><weather></${Hex.DYNAMIC}> " +
                    "for <${Hex.DYNAMIC}><duration></${Hex.DYNAMIC}> seconds</${Hex.SUCCESS}>",
                Placeholder.unparsed("world", world),
                Placeholder.unparsed("weather", weather),
                Placeholder.unparsed("duration", durationSeconds.toString()),
            )
        }

        fun playerWeatherNormal(name: String) = mm(
            "<${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> uses the world's weather</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        fun playerWeatherCurrent(
            name: String,
            weather: String,
        ) = mm(
            "<${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s personal weather is " +
                "<${Hex.DYNAMIC}><weather></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("weather", weather),
        )

        fun playerWeatherSet(
            name: String,
            weather: String,
        ) = mm(
            "<${Hex.SUCCESS}>Set <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s personal weather to " +
                "<${Hex.DYNAMIC}><weather></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("weather", weather),
        )

        fun playerWeatherReset(name: String) = mm(
            "<${Hex.SUCCESS}>Reset <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}>'s personal weather to the world's weather</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun errorInvalidTime(time: String) = mm(
            "<${Hex.ERROR}>Invalid time: <${Hex.DYNAMIC}><time></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("time", time),
        )

        fun errorWorldNotFound(world: String) = mm(
            "<${Hex.ERROR}>World <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}> was not found</${Hex.ERROR}>",
            Placeholder.unparsed("world", world),
        )

        val errorWeatherWorldRequired = mm("<${Hex.ERROR}>Specify a world when using this command from the console</${Hex.ERROR}>")

        fun errorPlayerMaxHealthUnavailable(name: String) = mm(
            "<${Hex.ERROR}>Player <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> has no maximum-health attribute</${Hex.ERROR}>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Teleportation {
        val errorCouldNotFindSafeLocation = mm("<${Hex.ERROR}>Failed to find a safe location</${Hex.ERROR}>")

        val errorTeleportPermission = mm("<${Hex.ERROR}>You do not have permission to use this teleport option</${Hex.ERROR}>")

        val errorTeleportSelf = mm("<${Hex.ERROR}>You cannot teleport to yourself</${Hex.ERROR}>")

        val errorTeleportRequestPending = mm("<${Hex.ERROR}>You already have an outgoing teleport request</${Hex.ERROR}>")

        val errorTeleportRequestMissing = mm("<${Hex.ERROR}>No matching teleport request was found</${Hex.ERROR}>")

        val errorTeleportRequestAmbiguous = mm("<${Hex.ERROR}>You have multiple incoming requests; specify a player</${Hex.ERROR}>")

        val errorTeleportRequestExpired = mm("<${Hex.ERROR}>That teleport request has expired</${Hex.ERROR}>")

        val errorTeleportHistoryMissing = mm("<${Hex.ERROR}>No previous location is available</${Hex.ERROR}>")

        val errorTeleportHistoryWorldUnavailable = mm("<${Hex.ERROR}>The world for your previous location is unavailable</${Hex.ERROR}>")

        val errorTeleportFailed = mm("<${Hex.ERROR}>The teleport failed</${Hex.ERROR}>")

        val errorTeleportTargetOffline = mm("<${Hex.ERROR}>That player is no longer online</${Hex.ERROR}>")

        val errorTeleportCoordinatesInvalid = mm("<${Hex.ERROR}>Coordinates must be finite numbers</${Hex.ERROR}>")

        val errorTeleportExplicitTargetRequired = mm("<${Hex.ERROR}>Specify an online destination player</${Hex.ERROR}>")

        val searchingForSafeLocation = mm("<${Hex.SECONDARY}><i>Searching for a safe location...</i></${Hex.SECONDARY}>")

        fun teleportRequestReceived(name: String) = mm(
            "<${Hex.WARNING}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> wants to teleport to you. Use " +
                "<click:run_command:'/tp accept'><hover:show_text:'Click to accept'>" +
                "<${Hex.LINK}><underlined>/tp accept</underlined></${Hex.LINK}></hover></click> or " +
                "<click:run_command:'/tp deny'><hover:show_text:'Click to deny'>" +
                "<${Hex.LINK}><underlined>/tp deny</underlined></${Hex.LINK}></hover></click>.</${Hex.WARNING}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportHereRequestReceived(name: String) = mm(
            "<${Hex.WARNING}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> wants you to teleport to them. Use " +
                "<click:run_command:'/tp accept'><hover:show_text:'Click to accept'>" +
                "<${Hex.LINK}><underlined>/tp accept</underlined></${Hex.LINK}></hover></click> or " +
                "<click:run_command:'/tp deny'><hover:show_text:'Click to deny'>" +
                "<${Hex.LINK}><underlined>/tp deny</underlined></${Hex.LINK}></hover></click>.</${Hex.WARNING}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestAccepted(name: String) = mm(
            "<${Hex.SUCCESS}>Teleport request from <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> accepted</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestDenied(name: String) = mm(
            "<${Hex.ERROR}>Teleport request from <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> denied</${Hex.ERROR}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestWasDenied(name: String) = mm(
            "<${Hex.ERROR}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> denied your teleport request</${Hex.ERROR}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestCancelled(name: String) = mm(
            "<${Hex.WARNING}>Teleport request to <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> cancelled</${Hex.WARNING}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestWasCancelled(name: String) = mm(
            "<${Hex.WARNING}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> cancelled their teleport request</${Hex.WARNING}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestExpired(name: String) = mm(
            "<${Hex.WARNING}>Teleport request from <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> expired</${Hex.WARNING}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestSentExpired(name: String) = mm(
            "<${Hex.WARNING}>Your teleport request to <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> expired</${Hex.WARNING}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestDisconnected(name: String) = mm(
            "<${Hex.WARNING}>Teleport request involving <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> was " +
                "removed because a player disconnected</${Hex.WARNING}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportRequestCreated(
            name: String,
            ttl: Duration,
        ) = mm(
            "<${Hex.SUCCESS}>Teleport request to <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> created " +
                "(expires in <${Hex.DYNAMIC}><seconds></${Hex.DYNAMIC}> seconds)</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("seconds", ttl.inWholeSeconds.toString()),
        )

        fun teleportCompleted(name: String) = mm(
            "<${Hex.SUCCESS}>Teleported to <${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportOperationSucceeded(
            player: String,
            target: String,
        ) = mm(
            "<${Hex.SUCCESS}>Teleported <${Hex.DYNAMIC}><player></${Hex.DYNAMIC}> to " +
                "<${Hex.DYNAMIC}><target></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("player", player),
            Placeholder.unparsed("target", target),
        )

        fun teleportReceived(name: String) = mm(
            "<${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> teleported to you</${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportBackCompleted(name: String) = mm(
            "<${Hex.SUCCESS}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> returned to their previous location</${Hex.SUCCESS}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportBulkSummary(
            succeeded: Int,
            failed: Int,
        ) = mm(
            "<${Hex.SUCCESS}>Teleported <${Hex.DYNAMIC}><succeeded></${Hex.DYNAMIC}> " +
                "player(s)</${Hex.SUCCESS}><${Hex.SECONDARY}>; <${Hex.DYNAMIC}><failed></${Hex.DYNAMIC}> " +
                "failed</${Hex.SECONDARY}>",
            Placeholder.unparsed("succeeded", succeeded.toString()),
            Placeholder.unparsed("failed", failed.toString()),
        )

        fun teleportRequestsCreated(count: Int) = mm(
            "<${Hex.SUCCESS}>Teleport requests sent to <${Hex.DYNAMIC}><count></${Hex.DYNAMIC}> player(s)</${Hex.SUCCESS}>",
            Placeholder.unparsed("count", count.toString()),
        )

        fun teleportedToCoordinate(
            x: Double,
            y: Double,
            z: Double,
        ) = mm(
            "<${Hex.SECONDARY}><i>⚡ Teleported to <b>{<${Hex.DYNAMIC}><x></${Hex.DYNAMIC}>, " +
                "<${Hex.DYNAMIC}><y></${Hex.DYNAMIC}>, " +
                "<${Hex.DYNAMIC}><z></${Hex.DYNAMIC}>}</b></i></${Hex.SECONDARY}>",
            Placeholder.unparsed("x", x.toString()),
            Placeholder.unparsed("y", y.toString()),
            Placeholder.unparsed("z", z.toString()),
        )

        fun teleportedToName(name: String) = mm(
            "<${Hex.SECONDARY}><i>⚡ Teleported to <b><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}></b></i></${Hex.SECONDARY}>",
            Placeholder.unparsed("name", name),
        )

        fun teleportedToCoordinate(
            x: Int,
            y: Int,
            z: Int,
        ) = mm(
            "<${Hex.SECONDARY}><i>⚡ Teleported to <b>{<${Hex.DYNAMIC}><x></${Hex.DYNAMIC}>, " +
                "<${Hex.DYNAMIC}><y></${Hex.DYNAMIC}>, " +
                "<${Hex.DYNAMIC}><z></${Hex.DYNAMIC}>}</b></i></${Hex.SECONDARY}>",
            Placeholder.unparsed("x", x.toString()),
            Placeholder.unparsed("y", y.toString()),
            Placeholder.unparsed("z", z.toString()),
        )
    }

    inner class Spawns {
        val errorHubWorldNotFound = mm("<${Hex.ERROR}>Could not find hub world</${Hex.ERROR}>")

        val errorHubNotSet = mm("<${Hex.ERROR}>Hub world not set</${Hex.ERROR}>")

        fun spawnSet(location: Location) = mm(
            "<${Hex.SUCCESS}>World spawn set to <${Hex.SECONDARY}>" +
                "{<${Hex.DYNAMIC}><x></${Hex.DYNAMIC}>, <${Hex.DYNAMIC}><y></${Hex.DYNAMIC}>, <${Hex.DYNAMIC}><z></${Hex.DYNAMIC}>} " +
                "pitch=<${Hex.DYNAMIC}><pitch></${Hex.DYNAMIC}> yaw=<${Hex.DYNAMIC}><yaw></${Hex.DYNAMIC}>" +
                "</${Hex.SECONDARY}></${Hex.SUCCESS}>",
            Placeholder.unparsed("x", location.x.toString()),
            Placeholder.unparsed("y", location.y.toString()),
            Placeholder.unparsed("z", location.z.toString()),
            Placeholder.unparsed("pitch", location.pitch.toString()),
            Placeholder.unparsed("yaw", location.yaw.toString()),
        )

        val teleportedToSpawn = mm("<${Hex.SECONDARY}><i>⚡ Teleported to spawn</i></${Hex.SECONDARY}>")

        val teleportedToHub = mm("<${Hex.SECONDARY}><i>⚡ Teleported to hub</i></${Hex.SECONDARY}>")
    }

    inner class Moderation {
        val kickedFromServer = mm("Kicked from server.")

        fun playerWasKickedForReason(
            name: String,
            reason: String,
        ) = mm(
            "<i><${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> was kicked: " +
                "<${Hex.DYNAMIC}><reason></${Hex.DYNAMIC}></${Hex.SECONDARY}></i>",
            Placeholder.unparsed("name", name),
            Placeholder.unparsed("reason", reason),
        )

        fun playerWasKicked(name: String) = mm(
            "<i><${Hex.SECONDARY}><${Hex.DYNAMIC}><name></${Hex.DYNAMIC}> was kicked</${Hex.SECONDARY}></i>",
            Placeholder.unparsed("name", name),
        )
    }

    inner class Connection {
        val errorUnableToAuthConnection =
            Component.text("Unable to authorize your connection. Please try again later.", errorColor)

        val playerStateUnavailableWarning =
            Component.text(
                "Warning: Unable to retrieve player data. Your permissions, rank, and other data may be missing. " +
                    "Please type /sync or try reconnecting in a moment.",
                warningColor,
            )
    }

    inner class ServerList {
        val defaultServerListMotd = Component.text("Project City Build")
    }

    inner class Tablist {
        fun playerPing(ping: Int) = mm(
            " <${Hex.SECONDARY}><${Hex.DYNAMIC}><ping></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("ping", ping.toString()),
        )
    }

    inner class Maintenance {
        val maintenanceConnectionDenied =
            mm("<bold>Server Maintenance</bold><newline><newline><${Hex.SECONDARY}>Please try again later</${Hex.SECONDARY}>")

        val maintenanceMotd = mm("<${Hex.WARNING}>Server maintenance - be right back!</${Hex.WARNING}>")

        val maintenanceReminder = mm("<italic><${Hex.WARNING}>[Reminder] Maintenance mode is currently ON</${Hex.WARNING}></italic>")

        fun maintenanceAlreadyInState(enabled: Boolean) = mm(
            "<${Hex.ERROR}>Maintenance mode is already <${Hex.DYNAMIC}><state></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("state", enabled.onOff()),
        )

        fun maintenanceModeChanged(enabled: Boolean) = mm(
            "<${Hex.SUCCESS}>Maintenance mode is now <${Hex.DYNAMIC}><state></${Hex.DYNAMIC}></${Hex.SUCCESS}>",
            Placeholder.unparsed("state", enabled.onOff()),
        )

        fun maintenanceStatus(enabled: Boolean) = mm(
            "<${Hex.SECONDARY}>Maintenance mode is currently <${Hex.DYNAMIC}><state></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("state", enabled.onOff()),
        )
    }

    inner class Worlds {
        val worldsCommandDescription = "Manages world settings"
        val worldLifecycleCommandDescription = "Manages registered worlds"
        val worldDeleteCommandDescription = "Deletes a world after explicit confirmation"
        val borderCommandDescription = "Sets and enforces a world's border"
        val borderSetCommandDescription = "Sets a square, circle, or rectangle border with an optional center"
        val borderFillCommandDescription = "Generates missing chunks inside the world border"
        val borderClearCommandDescription = "Clears the world's border"

        val borderNotConfigured = mm("<${Hex.ERROR}>No world border is configured for this world.</${Hex.ERROR}>")
        val borderCleared = mm("<${Hex.SUCCESS}>World border cleared.</${Hex.SUCCESS}>")
        val borderReached = mm("<${Hex.ERROR}>You have reached the world border.</${Hex.ERROR}>")

        fun worldList(names: String) = mm(
            "<${Hex.SECONDARY}>Registered worlds: <${Hex.DYNAMIC}><names></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("names", names),
        )

        fun worldOperationSucceeded(
            operation: String,
            world: String,
        ) = mm(
            "<${Hex.SUCCESS}><operation> world <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}>.</${Hex.SUCCESS}>",
            Placeholder.unparsed("operation", operation),
            Placeholder.unparsed("world", world),
        )

        fun worldOperationFailed(reason: String) = mm(
            "<${Hex.ERROR}>World operation failed: <${Hex.DYNAMIC}><reason></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("reason", reason),
        )

        fun borderStatus(
            shape: String,
            centerX: Double,
            centerZ: Double,
            radius: Double,
        ) = mm(
            "<${Hex.SECONDARY}>World border: <${Hex.DYNAMIC}><shape></${Hex.DYNAMIC}>, center " +
                "<${Hex.DYNAMIC}><x></${Hex.DYNAMIC}>, <${Hex.DYNAMIC}><z></${Hex.DYNAMIC}>; " +
                "radius <${Hex.DYNAMIC}><radius></${Hex.DYNAMIC}> blocks.</${Hex.SECONDARY}>",
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
            "<${Hex.SECONDARY}>World border: rectangle, center <${Hex.DYNAMIC}><x></${Hex.DYNAMIC}>, " +
                "<${Hex.DYNAMIC}><z></${Hex.DYNAMIC}>; " +
                "width <${Hex.DYNAMIC}><width></${Hex.DYNAMIC}>, depth <${Hex.DYNAMIC}><depth></${Hex.DYNAMIC}> blocks.</${Hex.SECONDARY}>",
            Placeholder.unparsed("x", centerX.toString()),
            Placeholder.unparsed("z", centerZ.toString()),
            Placeholder.unparsed("width", width.toString()),
            Placeholder.unparsed("depth", depth.toString()),
        )

        fun invalidBorder(reason: String?) = mm(
            "<${Hex.ERROR}>Invalid world border: <${Hex.DYNAMIC}><reason></${Hex.DYNAMIC}></${Hex.ERROR}>",
            Placeholder.unparsed("reason", reason ?: "unknown reason"),
        )

        fun borderSet(
            shape: String,
            centerX: Double,
            centerZ: Double,
            radius: Double,
        ) = mm(
            "<${Hex.SUCCESS}>World border set: <${Hex.DYNAMIC}><shape></${Hex.DYNAMIC}>, center " +
                "<${Hex.DYNAMIC}><x></${Hex.DYNAMIC}>, <${Hex.DYNAMIC}><z></${Hex.DYNAMIC}>; " +
                "radius <${Hex.DYNAMIC}><radius></${Hex.DYNAMIC}> blocks.</${Hex.SUCCESS}>",
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
            "<${Hex.SUCCESS}>World border set: rectangle, center " +
                "<${Hex.DYNAMIC}><x></${Hex.DYNAMIC}>, <${Hex.DYNAMIC}><z></${Hex.DYNAMIC}>; " +
                "width <${Hex.DYNAMIC}><width></${Hex.DYNAMIC}>, depth <${Hex.DYNAMIC}><depth></${Hex.DYNAMIC}> blocks.</${Hex.SUCCESS}>",
            Placeholder.unparsed("x", centerX.toString()),
            Placeholder.unparsed("z", centerZ.toString()),
            Placeholder.unparsed("width", width.toString()),
            Placeholder.unparsed("depth", depth.toString()),
        )

        fun borderRelocationFailures(count: Int) = mm(
            "<${Hex.WARNING}>World border was committed, but relocation failed for " +
                "<${Hex.DYNAMIC}><count></${Hex.DYNAMIC}> player(s). See server logs.</${Hex.WARNING}>",
            Placeholder.unparsed("count", count.toString()),
        )

        val borderRespawnRelocationFailed =
            mm("<${Hex.ERROR}>Could not safely relocate your respawn inside the world border. Please contact staff.</${Hex.ERROR}>")

        val fillRequiresBorder = mm("<${Hex.ERROR}>Set a world border before filling chunks.</${Hex.ERROR}>")

        fun fillLargeWarning(chunkCountUpperBound: Long) = mm(
            "<${Hex.WARNING}>This is a large fill (up to <${Hex.DYNAMIC}><count></${Hex.DYNAMIC}> chunks) and may take a long time. " +
                "Click <click:run_command:'/worlds border fill confirm'>" +
                "<hover:show_text:'Click to confirm the fill'><${Hex.LINK}><underlined>" +
                "/worlds border fill confirm</underlined></${Hex.LINK}></hover></click> to continue.</${Hex.WARNING}>",
            Placeholder.unparsed("count", chunkCountUpperBound.toString()),
        )

        fun fillPreview(
            chunkCountUpperBound: Long,
            worldName: String,
        ) = mm(
            "<${Hex.WARNING}>Fill preview: up to <${Hex.DYNAMIC}><count></${Hex.DYNAMIC}> chunks may " +
                "be checked in <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}>. " +
                "Only missing chunks will be generated. Click " +
                "<click:run_command:'/worlds border fill confirm'>" +
                "<hover:show_text:'Click to confirm the fill'><${Hex.LINK}><underlined>" +
                "/worlds border fill confirm</underlined></${Hex.LINK}></hover></click> within 60 seconds to continue.</${Hex.WARNING}>",
            Placeholder.unparsed("count", chunkCountUpperBound.toString()),
            Placeholder.unparsed("world", worldName),
        )

        val fillPreviewMissing = mm("<${Hex.ERROR}>There is no active fill preview. Run /worlds border fill first.</${Hex.ERROR}>")

        val fillWrongWorld = mm("<${Hex.ERROR}>You must confirm the fill in the world where it was previewed.</${Hex.ERROR}>")

        val fillBorderChanged = mm(
            "<${Hex.ERROR}>The world border changed. Run /worlds border fill again to preview the updated area.</${Hex.ERROR}>",
        )

        val fillAlreadyRunning = mm("<${Hex.ERROR}>A world border fill is already running for this world.</${Hex.ERROR}>")

        val fillStarted = mm("<${Hex.WARNING}>Starting world border fill...</${Hex.WARNING}>")

        val fillStoppedBorderChanged = mm("<${Hex.ERROR}>World border changed during fill; the operation was stopped.</${Hex.ERROR}>")

        fun fillProgress(
            checked: Long,
            chunkCountUpperBound: Long,
            generated: Long,
        ) = mm(
            "<${Hex.WARNING}>World border fill progress: checked " +
                "<${Hex.DYNAMIC}><checked></${Hex.DYNAMIC}> of up to " +
                "<${Hex.DYNAMIC}><total></${Hex.DYNAMIC}> chunks; " +
                "generated <${Hex.DYNAMIC}><generated></${Hex.DYNAMIC}>.</${Hex.WARNING}>",
            Placeholder.unparsed("checked", checked.toString()),
            Placeholder.unparsed("total", chunkCountUpperBound.toString()),
            Placeholder.unparsed("generated", generated.toString()),
        )

        fun fillComplete(
            checked: Long,
            chunkCountUpperBound: Long,
            generated: Long,
        ) = mm(
            "<${Hex.SUCCESS}>World border fill complete: checked <${Hex.DYNAMIC}><checked></${Hex.DYNAMIC}> chunks " +
                "(up to <${Hex.DYNAMIC}><total></${Hex.DYNAMIC}> in the border); generated " +
                "<${Hex.DYNAMIC}><generated></${Hex.DYNAMIC}>.</${Hex.SUCCESS}>",
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
            "<${Hex.WARNING}>World border fill <${Hex.DYNAMIC}><status></${Hex.DYNAMIC}>: checked " +
                "<${Hex.DYNAMIC}><checked></${Hex.DYNAMIC}> of up to " +
                "<${Hex.DYNAMIC}><total></${Hex.DYNAMIC}> chunks; generated <${Hex.DYNAMIC}><generated></${Hex.DYNAMIC}>.</${Hex.WARNING}>",
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
            "<${Hex.WARNING}>World border fill cancelled: checked <${Hex.DYNAMIC}><checked></${Hex.DYNAMIC}> of up to " +
                "<${Hex.DYNAMIC}><total></${Hex.DYNAMIC}> chunks; generated <${Hex.DYNAMIC}><generated></${Hex.DYNAMIC}>.</${Hex.WARNING}>",
            Placeholder.unparsed("checked", checked.toString()),
            Placeholder.unparsed("total", chunkCountUpperBound.toString()),
            Placeholder.unparsed("generated", generated.toString()),
        )

        val fillFailed = mm("<${Hex.ERROR}>World border fill failed. See the server log for details.</${Hex.ERROR}>")
        val fillNoActiveJob = mm("<${Hex.ERROR}>There is no world border fill running in this world.</${Hex.ERROR}>")
        val fillCancelRequested = mm("<${Hex.WARNING}>World border fill cancellation requested.</${Hex.WARNING}>")
    }

    inner class Portals {
        val commandDescription = "Manages Nether and End portal links"

        fun linkCreated(
            source: String,
            type: String,
            destination: String,
            bidirectional: Boolean,
        ) = mm(
            "<${Hex.SUCCESS}>Linked <${Hex.DYNAMIC}><source></${Hex.DYNAMIC}> " +
                "<${Hex.DYNAMIC}><type></${Hex.DYNAMIC}> portals to " +
                "<${Hex.DYNAMIC}><destination></${Hex.DYNAMIC}>" +
                if (bidirectional) " in both directions.</${Hex.SUCCESS}>" else ".</${Hex.SUCCESS}>",
            Placeholder.unparsed("source", source),
            Placeholder.unparsed("type", type),
            Placeholder.unparsed("destination", destination),
        )

        fun linkRemoved(source: String, type: String) = mm(
            "<${Hex.SUCCESS}>Removed the <${Hex.DYNAMIC}><type></${Hex.DYNAMIC}> portal link for " +
                "<${Hex.DYNAMIC}><source></${Hex.DYNAMIC}>.</${Hex.SUCCESS}>",
            Placeholder.unparsed("source", source),
            Placeholder.unparsed("type", type),
        )

        fun noLink(source: String, type: String) = mm(
            "<${Hex.SECONDARY}>No <${Hex.DYNAMIC}><type></${Hex.DYNAMIC}> portal link is configured for " +
                "<${Hex.DYNAMIC}><source></${Hex.DYNAMIC}>.</${Hex.SECONDARY}>",
            Placeholder.unparsed("source", source),
            Placeholder.unparsed("type", type),
        )

        fun links(list: String) = mm(
            "<${Hex.SECONDARY}>Portal links: <${Hex.DYNAMIC}><links></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("links", list),
        )

        fun destinationUnavailable(world: String) = mm(
            "<${Hex.ERROR}>Portal destination <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}> is unavailable.</${Hex.ERROR}>",
            Placeholder.unparsed("world", world),
        )

        fun noDestination(world: String) = mm(
            "<${Hex.WARNING}>No portal destination is configured for <${Hex.DYNAMIC}><world></${Hex.DYNAMIC}>.</${Hex.WARNING}>",
            Placeholder.unparsed("world", world),
        )

        val disabled = mm("<${Hex.WARNING}>Portals are disabled for this destination.</${Hex.WARNING}>")
    }

    inner class Inventories {
        val commandDescription = "Inspects Multiverse-Inventories compatibility data"

        fun compatibilityReport(
            worlds: Int,
            groups: Int,
            global: Int,
            malformed: Int,
        ) = mm(
            "<${Hex.SECONDARY}>Multiverse-Inventories files: <${Hex.DYNAMIC}><worlds></${Hex.DYNAMIC}> " +
                "world profiles, <${Hex.DYNAMIC}><groups></${Hex.DYNAMIC}> group profiles, " +
                "<${Hex.DYNAMIC}><global></${Hex.DYNAMIC}> global profiles; " +
                "<${Hex.DYNAMIC}><malformed></${Hex.DYNAMIC}> malformed JSON/YAML files.</${Hex.SECONDARY}>",
            Placeholder.unparsed("worlds", worlds.toString()),
            Placeholder.unparsed("groups", groups.toString()),
            Placeholder.unparsed("global", global.toString()),
            Placeholder.unparsed("malformed", malformed.toString()),
        )

        fun inventoryGroups(groups: String) = mm(
            "<${Hex.SECONDARY}>Inventory groups: <${Hex.DYNAMIC}><groups></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("groups", groups),
        )

        fun inventoryConflicts(conflicts: String) = mm(
            "<${Hex.SECONDARY}>Overlapping group shares: <${Hex.DYNAMIC}><conflicts></${Hex.DYNAMIC}></${Hex.SECONDARY}>",
            Placeholder.unparsed("conflicts", conflicts),
        )
    }
}
