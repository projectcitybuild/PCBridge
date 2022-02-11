package com.projectcitybuild.modules.permissions.adapters

import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.permissions.Permissions
import dagger.Reusable
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

@Reusable
class LuckPermsPermissions @Inject constructor(
    private val logger: PlatformLogger,
): Permissions {

    class PermissionUserNotFoundException(): Exception()

    private lateinit var luckPerms: LuckPerms

    override fun bootstrap() {
        luckPerms = LuckPermsProvider.get()
    }

    override fun setUserGroups(playerUUID: UUID, groupNames: List<String>) {
        val user = luckPerms.userManager.getUser(playerUUID)
        if (user == null) {
            logger.fatal("Could not load user ($playerUUID) from permissions manager")
            throw PermissionUserNotFoundException()
        }

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
}