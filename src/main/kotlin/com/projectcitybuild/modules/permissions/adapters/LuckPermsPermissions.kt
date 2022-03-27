package com.projectcitybuild.modules.permissions.adapters

import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.permissions.Permissions
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import java.util.UUID
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LuckPermsPermissions @Inject constructor(
    private val logger: PlatformLogger,
) : Permissions {

    class PermissionUserNotFoundException() : Exception()

    private lateinit var luckPerms: LuckPerms

    override fun connect() {
        luckPerms = LuckPermsProvider.get()

        logger.info("Connected to LuckPerms")
    }

    private fun getUser(playerUUID: UUID): User {
        val user = luckPerms.userManager.getUser(playerUUID)
        if (user == null) {
            logger.fatal("Could not load user ($playerUUID) from permissions manager")
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

    override fun <T> getUserMetadata(playerUUID: UUID, key: String, valueTransformer: (String) -> T): T? {
        val user = getUser(playerUUID)
        val metadataNode = user.cachedData.metaData.getMetaValue(key, valueTransformer)

        return metadataNode.get()
    }

    override fun getGroupDisplayName(groupName: String): String? {
        // TODO: find better way to get Display Name node
        return luckPerms.groupManager.getGroup(groupName)?.displayName
    }

    override fun hasPermission(playerUUID: UUID, permission: String): Boolean {
        val user = getUser(playerUUID)
        val permissionNode = user.cachedData.permissionData.checkPermission(permission)

        return permissionNode.asBoolean()
    }
}
