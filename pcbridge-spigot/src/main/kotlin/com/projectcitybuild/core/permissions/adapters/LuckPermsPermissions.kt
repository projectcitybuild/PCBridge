package com.projectcitybuild.core.permissions.adapters

import com.projectcitybuild.core.permissions.Permissions
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import java.util.UUID
import java.util.stream.Collectors

class LuckPermsPermissions(
    private val logger: PlatformLogger,
) : Permissions {
    class PermissionUserNotFoundException() : Exception()

    private val luckPerms: LuckPerms
        get() = LuckPermsProvider.get()

    private fun getUser(playerUUID: UUID): User {
        val user = luckPerms.userManager.getUser(playerUUID)
        if (user == null) {
            logger.severe("Could not load user ($playerUUID) from permissions manager")
            throw PermissionUserNotFoundException()
        }
        return user
    }

    override fun setUserGroups(playerUUID: UUID, groupNames: List<String>) {
        val user = getUser(playerUUID)

        user.nodes.stream()
            .filter(NodeType.INHERITANCE::matches)
            .map(NodeType.INHERITANCE::cast)
            .collect(Collectors.toSet())
            .forEach { groupNode ->
                user.data().remove(groupNode)
            }

        groupNames.forEach { groupName ->
            val groupNode = InheritanceNode.builder(groupName).build()
            user.data().add(groupNode)

            logger.verbose("Assigning to $groupName group")
        }

        luckPerms.userManager.saveUser(user)
    }

    override fun getUserGroups(playerUUID: UUID): Set<String> {
        val user = getUser(playerUUID)

        return user.nodes.stream()
            .filter(NodeType.INHERITANCE::matches)
            .map(NodeType.INHERITANCE::cast)
            .map { it.groupName }
            .collect(Collectors.toSet())
            .toSet()
    }

    override fun getUserPrefix(playerUUID: UUID): String {
        val user = getUser(playerUUID)

        return user.nodes.stream()
            .filter(NodeType.PREFIX::matches)
            .map(NodeType.PREFIX::cast)
            .map { node -> node.metaValue }
            .collect(Collectors.toSet())
            .joinToString(separator = "")
    }

    override fun getUserSuffix(playerUUID: UUID): String {
        val user = getUser(playerUUID)

        return user.nodes.stream()
            .filter(NodeType.SUFFIX::matches)
            .map(NodeType.SUFFIX::cast)
            .map { node -> node.metaValue }
            .collect(Collectors.toSet())
            .joinToString(separator = "")
    }

    override fun getGroupMetaData(groupName: String, key: String): String? {
        val group = luckPerms.groupManager.getGroup(groupName)
            ?: return null

        return group.cachedData.metaData.getMetaValue(key)
    }

    override fun getGroupDisplayName(groupName: String): String? {
        // TODO: find better way to get Display Name node
        return luckPerms.groupManager.getGroup(groupName)?.displayName
    }
}
