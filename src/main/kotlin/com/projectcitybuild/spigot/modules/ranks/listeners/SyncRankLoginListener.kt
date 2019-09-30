package com.projectcitybuild.spigot.modules.ranks.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.entities.models.ApiResponse
import com.projectcitybuild.entities.models.AuthPlayerGroups
import com.projectcitybuild.spigot.modules.ranks.RankMapper
import me.lucko.luckperms.api.Node
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import retrofit2.Response
import java.util.*
import java.util.stream.Collectors

class SyncRankLoginListener : Listenable<PlayerJoinEvent> {
    override var environment: EnvironmentProvider? = null

    @EventHandler(priority = EventPriority.HIGH)
    override fun observe(event: PlayerJoinEvent) {
        syncRankWithServer(event.player)
    }

    private fun syncRankWithServer(player: Player) {
        val environment = environment ?: throw Exception("EnvironmentProvider has already been deallocated")
        val permissions = environment.permissions ?: throw Exception("Permission plugin is null")

        getPlayerGroups(playerId = player.uniqueId) { result ->
            val json = result.body()

            if (json?.error != null) {
                if (json.error.id != "account_not_linked") {
                    environment.sync {
                        player.sendMessage("Failed to sync rank: Trouble communicating with the authentication server...")
                    }
                }
                return@getPlayerGroups
            }

            environment.sync {
                val lpUser = permissions.userManager.getUser(player.uniqueId)
                if (lpUser == null) {
                    player.sendMessage("Sync failed: Could not load user from permission system. Please contact a staff member")
                    throw Exception("Could not load user from LuckPerms")
                }

                // Remove all groups from the player before syncing
                lpUser.allNodes.stream()
                        .filter(Node::isGroupNode)
                        .collect(Collectors.toSet())
                        .forEach { groupNode ->
                            lpUser.unsetPermission(groupNode)
                        }

                if (json?.data == null) {
                    return@sync
                }

                val permissionGroups = RankMapper.mapGroupsToPermissionGroups(json.data.groups)
                permissionGroups.forEach { group ->
                    val groupNode = permissions.nodeFactory.makeGroupNode(group).build()
                    if (!lpUser.hasPermission(groupNode).asBoolean()) {
                        lpUser.setPermission(groupNode)
                    }
                }

                permissions.userManager.saveUser(lpUser)
            }
        }
    }

    private fun getPlayerGroups(playerId: UUID, completion: (Response<ApiResponse<AuthPlayerGroups>>) -> Unit) {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")
        val authApi = environment.apiClient.authApi

        environment.async<Response<ApiResponse<AuthPlayerGroups>>> { resolve ->
            val request = authApi.getUserGroups(uuid = playerId.toString())
            val response = request.execute()

            resolve(response)
        }.startAndSubscribe(completion)
    }
}