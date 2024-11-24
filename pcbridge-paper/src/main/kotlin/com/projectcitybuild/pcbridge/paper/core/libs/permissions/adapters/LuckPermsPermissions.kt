package com.projectcitybuild.pcbridge.paper.core.libs.permissions.adapters

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.Permissions
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import java.util.UUID
import java.util.stream.Collectors

class LuckPermsPermissions : Permissions {
    class PermissionUserNotFoundException() : Exception()

    private val luckPerms: LuckPerms
        get() = LuckPermsProvider.get()

    private fun getUser(playerUUID: UUID): User {
        val user = luckPerms.userManager.getUser(playerUUID)
        if (user == null) {
            log.error { "Could not load user ($playerUUID) from permissions manager" }
            throw PermissionUserNotFoundException()
        }
        return user
    }

    override fun setUserGroups(
        playerUUID: UUID,
        groupNames: List<String>,
    ) {
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
            log.debug { "Assigning to $groupName group" }
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
}
