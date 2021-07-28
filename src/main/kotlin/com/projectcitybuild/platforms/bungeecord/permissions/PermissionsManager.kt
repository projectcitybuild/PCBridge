package com.projectcitybuild.platforms.bungeecord.permissions

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import java.util.*
import java.util.stream.Collectors

class PermissionsManager {

    private val plugin = getPermissionPlugin()

    private fun getPermissionPlugin(): LuckPerms {
        return LuckPermsProvider.get()
    }

    fun getUser(uuid: UUID): PermissionsUser? {
        val user = plugin.userManager.getUser(uuid)
            ?: throw Exception("Could not load user from LuckPerms (uuid: ${uuid})")

        return PermissionsUser(user)
    }

    fun getGroup(name: String): PermissionsGroup {
        val groupNode = InheritanceNode.builder(name).build()
        return PermissionsGroup(groupNode)
    }

    fun saveChanges(user: PermissionsUser) {
        plugin.userManager.saveUser(user.user)
    }
}

class PermissionsUser(
    val user: User
) {
    fun removeAllGroups() {
        user.nodes.stream()
            .filter(NodeType.INHERITANCE::matches)
            .map(NodeType.INHERITANCE::cast)
            .collect(Collectors.toSet())
            .forEach { groupNode ->
                user.data().remove(groupNode)
            }
    }

    fun addGroup(group: PermissionsGroup) {
        user.data().add(group.node)
    }

    fun hasGroup(group: PermissionsGroup): Boolean {
        return user.nodes.contains(group.node)
    }

    fun hasPermission(permission: String): Boolean {
        val node = Node.builder(permission).build()
        return user.nodes.contains(node)
    }
}

class PermissionsGroup(
    val node: InheritanceNode
) {

}