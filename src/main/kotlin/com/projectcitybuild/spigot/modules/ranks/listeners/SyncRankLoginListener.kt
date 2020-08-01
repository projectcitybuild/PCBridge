package com.projectcitybuild.spigot.modules.ranks.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.entities.models.ApiResponse
import com.projectcitybuild.entities.models.AuthPlayerGroups
import com.projectcitybuild.spigot.modules.ranks.RankMapper
import net.luckperms.api.node.Node
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
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
                lpUser.nodes.stream()
                        .filter(NodeType.INHERITANCE::matches)
                        .map(NodeType.INHERITANCE::cast)
                        .collect(Collectors.toSet())
                        .forEach { groupNode ->
                            lpUser.data().remove(groupNode)
                        }

                if (json?.data == null) {
                    val groupNode = InheritanceNode.builder("guest").build()
                    lpUser.data().add(groupNode)
                    return@sync
                }

                val permissionGroups = RankMapper.mapGroupsToPermissionGroups(json.data.groups)
                permissionGroups.forEach { group ->
                    val groupNode = InheritanceNode.builder(group).build()
                    if (!lpUser.nodes.contains(groupNode)) {
                        lpUser.data().add(groupNode)
                    }
                }

                // Just in case, assign to Guest if no groups available (shouldn't happen though)
                if (permissionGroups.isEmpty()) {
                    val groupNode = InheritanceNode.builder("guest").build()
                    lpUser.data().add(groupNode)
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