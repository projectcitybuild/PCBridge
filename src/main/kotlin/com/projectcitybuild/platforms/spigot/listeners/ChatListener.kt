package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.entities.BuildGroup
import com.projectcitybuild.core.entities.DonorGroup
import com.projectcitybuild.core.entities.TrustGroup
import com.projectcitybuild.core.utilities.PlayerStore
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import net.luckperms.api.node.NodeType
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.stream.Collectors


/**
 * FIXME: Awful hacky, hardcoded stuff in here to save time
 */
class ChatListener(
        private val playerStore: PlayerStore,
        private val permissionsManager: PermissionsManager,
        private val logger: LoggerProvider
): Listener {
    data class Group<GroupType>(
            val group: GroupType,
            val displayName: String,
            val hoverName: String
    )
    private val trustGroupPriority: HashMap<TrustGroup, Int> = hashMapOf(
            TrustGroup.GUEST to 0,
            TrustGroup.MEMBER to 1,
            TrustGroup.RETIRED to 2,
            TrustGroup.TRUSTED to 3,
            TrustGroup.TRUSTED_PLUS to 4,
            TrustGroup.MODERATOR to 5,
            TrustGroup.OPERATOR to 6,
            TrustGroup.SENIOR_OPERATOR to 7,
            TrustGroup.ADMINISTRATOR to 8
    )
    private val buildGroupPriority: HashMap<BuildGroup, Int> = hashMapOf(
            BuildGroup.NONE to 0,
            BuildGroup.INTERN to 1,
            BuildGroup.BUILDER to 2,
            BuildGroup.PLANNER to 3,
            BuildGroup.ENGINEER to 4,
            BuildGroup.ARCHITECT to 5
    )

    private fun highestTrustGroup(lhs: Group<TrustGroup>, rhs: Group<TrustGroup>): Group<TrustGroup> {
        val leftPriority = trustGroupPriority[lhs.group] ?: 0
        val rightPriority = trustGroupPriority[rhs.group] ?: 0

        if (leftPriority > rightPriority) {
            return lhs
        }
        return rhs
    }

    private fun highestBuildGroup(lhs: Group<BuildGroup>, rhs: Group<BuildGroup>): Group<BuildGroup> {
        val leftPriority = buildGroupPriority[lhs.group] ?: -1
        val rightPriority = buildGroupPriority[rhs.group] ?: -1

        if (leftPriority > rightPriority) {
            return lhs
        }
        return rhs
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun observe(event: AsyncPlayerChatEvent) {
        // Mute player if necessary
        val sendingPlayer = playerStore.get(event.player.uniqueId)
        if (sendingPlayer?.isMuted == true) {
            event.isCancelled = true
            event.player.sendMessage("You cannot chat while muted")
            return
        }

        // Format user display name
        val lpUser = permissionsManager.getUser(event.player.uniqueId)
                ?: throw Exception("Could not load user from LuckPerms")

        val groupNodes = lpUser.nodes.stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .collect(Collectors.toSet())

        val prefixes = lpUser.nodes.stream()
                .filter(NodeType.PREFIX::matches)
                .map(NodeType.PREFIX::cast)
                .map { node -> node.metaValue }
                .collect(Collectors.toSet())
                .joinToString(separator = "")

        val suffixes = lpUser.nodes.stream()
                .filter(NodeType.SUFFIX::matches)
                .map(NodeType.SUFFIX::cast)
                .map { node -> node.metaValue }
                .collect(Collectors.toSet())
                .joinToString(separator = "")

        // FIXME: cache so that this isn't performed every time a message is sent
        var donorGroup = Group(DonorGroup.NONE, "", "")
        var buildGroup = Group(BuildGroup.NONE, "", "")
        var trustGroup = Group(TrustGroup.GUEST, "", "")

        groupNodes.forEach { groupNode ->
            val group = permissionsManager.getGroup(groupNode.groupName)
            val displayName = group?.displayName ?: groupNode.groupName

            // FIXME: hardcoded for the sake of time, but this should all be from an API
            val groupName = groupNode.groupName.toLowerCase()

            when (groupName) {
                "donator" -> donorGroup = Group(DonorGroup.DONOR, displayName, "Donor")
                "member" -> {
                    val newGroup = Group(TrustGroup.MEMBER, displayName, "")
                    trustGroup = highestTrustGroup(trustGroup, newGroup)
                }
                "trusted" -> {
                    val newGroup = Group(TrustGroup.TRUSTED, displayName, "Trusted")
                    trustGroup = highestTrustGroup(trustGroup, newGroup)
                }
                "trusted+" -> {
                    val newGroup = Group(TrustGroup.TRUSTED_PLUS, displayName, "Trusted+")
                    trustGroup = highestTrustGroup(trustGroup, newGroup)
                }
                "moderator" -> {
                    val newGroup = Group(TrustGroup.MODERATOR, "§e[Staff]", "Moderator")
                    trustGroup = highestTrustGroup(trustGroup, newGroup)
                }
                "op" -> {
                    val newGroup = Group(TrustGroup.OPERATOR, "§6[Staff]", "Operator")
                    trustGroup = highestTrustGroup(trustGroup, newGroup)
                }
                "sop" -> {
                    val newGroup = Group(TrustGroup.SENIOR_OPERATOR, "§c[Staff]", "Senior Operator")
                    trustGroup = highestTrustGroup(trustGroup, newGroup)
                }
                "admin" -> {
                    val newGroup = Group(TrustGroup.ADMINISTRATOR, "§4[Staff]", "Administrator")
                    trustGroup = highestTrustGroup(trustGroup, newGroup)
                }
                "retired" -> {
                    val newGroup = Group(TrustGroup.RETIRED, displayName, "Retired Staff")
                    trustGroup = highestTrustGroup(trustGroup, newGroup)
                }
                "intern" -> {
                    val newGroup = Group(BuildGroup.INTERN, displayName, "Intern")
                    buildGroup = highestBuildGroup(buildGroup, newGroup)
                }
                "builder" -> {
                    val newGroup = Group(BuildGroup.BUILDER, displayName, "Builder")
                    buildGroup = highestBuildGroup(buildGroup, newGroup)
                }
                "planner" -> {
                    val newGroup = Group(BuildGroup.PLANNER, displayName, "Planner")
                    buildGroup = highestBuildGroup(buildGroup, newGroup)
                }
                "engineer" -> {
                    val newGroup = Group(BuildGroup.ENGINEER, displayName, "Engineer")
                    buildGroup = highestBuildGroup(buildGroup, newGroup)
                }
                "architect" -> {
                    val newGroup = Group(BuildGroup.ARCHITECT, displayName, "Architect")
                    buildGroup = highestBuildGroup(buildGroup, newGroup)
                }
            }
        }

        val groupTC = TextComponent()
        if (donorGroup.group != DonorGroup.NONE) {
            TextComponent
                    .fromLegacyText(donorGroup.displayName)
                    .forEach { c ->
                        val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(donorGroup.hoverName).create())
                        c.hoverEvent = hoverEvent
                        groupTC.addExtra(c)
                    }
        }
        if (trustGroup.group != TrustGroup.GUEST && trustGroup.displayName.isNotBlank()) {
            TextComponent
                    .fromLegacyText(trustGroup.displayName)
                    .forEach { c ->
                        val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(trustGroup.hoverName).create())
                        c.hoverEvent = hoverEvent
                        groupTC.addExtra(c)
                    }
        }
        if (buildGroup.group != BuildGroup.NONE && buildGroup.displayName.isNotBlank()) {
            TextComponent
                    .fromLegacyText(buildGroup.displayName)
                    .forEach { c ->
                        val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(buildGroup.hoverName).create())
                        c.hoverEvent = hoverEvent
                        groupTC.addExtra(c)
                    }
        }

        val messageTC = TextComponent.fromLegacyText(event.message)

        val whitespaceResetTC = TextComponent(" ")
        whitespaceResetTC.color = ChatColor.RESET

        val colonTC = TextComponent(": ")
        colonTC.color = ChatColor.RESET

        // Dynamic text from other plugins (eg. LuckyPerms) contains legacy color codes
        val prefixTC = TextComponent.fromLegacyText(prefixes)
        val suffixTC = TextComponent.fromLegacyText(suffixes)
        val displayNameTC = TextComponent.fromLegacyText(event.player.displayName)

        val textComponent = TextComponent()
        prefixTC.forEach { c -> textComponent.addExtra(c) }
        textComponent.addExtra(groupTC)
        textComponent.addExtra(whitespaceResetTC)
        displayNameTC.forEach { c -> textComponent.addExtra(c)}
        suffixTC.forEach { c -> textComponent.addExtra(c) }
        textComponent.addExtra(colonTC)
        messageTC.forEach { c -> textComponent.addExtra(c) }

        event.isCancelled = true

        event.recipients.forEach { player ->
            player.spigot().sendMessage(textComponent)
        }

        // Messages sent to users don't appear in console, so we have to log it manually
        logger.info("<${event.player.displayName}> ${event.message}")
    }

}