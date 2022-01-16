package com.projectcitybuild.modules.permissions

import dagger.Reusable
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

@Reusable
class PermissionsManager @Inject constructor() {

    val plugin = getPermissionPlugin()

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

    fun groups(): Set<PermissionsGroup> {
        return user.nodes.stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map { PermissionsGroup(node = it) }
                .collect(Collectors.toSet())
                .toSet()
    }

    fun prefixes(): String {
        return user.nodes.stream()
                .filter(NodeType.PREFIX::matches)
                .map(NodeType.PREFIX::cast)
                .map { node -> node.metaValue }
                .collect(Collectors.toSet())
                .joinToString(separator = "")
    }

    fun suffixes(): String {
        return user.nodes.stream()
                .filter(NodeType.SUFFIX::matches)
                .map(NodeType.SUFFIX::cast)
                .map { node -> node.metaValue }
                .collect(Collectors.toSet())
                .joinToString(separator = "")
    }
}

class PermissionsGroup(
    val node: InheritanceNode
) {
    val name: String
        get() = node.groupName

    fun getDisplayName(permissionsManager: PermissionsManager): String? {
        // TODO: find better way to get Display Name node
        return permissionsManager.plugin.groupManager.getGroup(name)?.displayName
    }
}