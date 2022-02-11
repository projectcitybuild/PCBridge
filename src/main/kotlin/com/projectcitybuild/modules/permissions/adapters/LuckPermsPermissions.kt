package com.projectcitybuild.modules.permissions.adapters

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroupsUseCase
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.permissions.Permissions
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import java.util.*
import javax.inject.Inject

class LuckPermsPermissions @Inject constructor(
    private val logger: PlatformLogger,
): Permissions {

    private lateinit var luckPerms: LuckPerms

    override fun bootstrap() {
        luckPerms = LuckPermsProvider.get()
    }

    override fun setUserGroups(playerUUID: UUID, groups: List<String>) {
        val user = luckPerms.userManager.getUser(uuid)
        if (user == null) {
            logger.warning("Could not load user from permissions manager (UUID: ${playerUUID})")
            return Failure(UpdatePlayerGroupsUseCase.FailureReason.PERMISSION_USER_NOT_FOUND)
        }
    }
}