package com.projectcitybuild.pcbridge.paper.integrations.luckperms

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionsProvider
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.user.User
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import java.util.UUID
import java.util.stream.Collectors

class LuckPermsPermissionsProvider(
    private val luckPerms: LuckPerms,
): PermissionsProvider {
    class PermissionUserNotFoundException() : Exception()

    override fun getUserRoles(playerUUID: UUID): Set<String> {
        val user = getUser(playerUUID)

        return user.nodes.stream()
            .filter(NodeType.INHERITANCE::matches)
            .map(NodeType.INHERITANCE::cast)
            .map { it.groupName }
            .collect(Collectors.toSet())
            .toSet()
    }

    override fun setUserRoles(
        playerUUID: UUID,
        roleNames: Set<String>,
    ) {
        val user = getUser(playerUUID)

        user.nodes.stream()
            .filter(NodeType.INHERITANCE::matches)
            .map(NodeType.INHERITANCE::cast)
            .collect(Collectors.toSet())
            .forEach { groupNode ->
                user.data().remove(groupNode)
            }

        roleNames.forEach { groupName ->
            val groupNode = InheritanceNode.builder(groupName).build()
            user.data().add(groupNode)
            log.debug { "Assigning to $groupName group" }
        }

        luckPerms.userManager.saveUser(user)
    }

    private fun getUser(playerUUID: UUID): User {
        val user = luckPerms.userManager.getUser(playerUUID)
        if (user == null) {
            log.error { "Could not load user ($playerUUID) from permissions manager" }
            throw PermissionUserNotFoundException()
        }
        return user
    }
}
