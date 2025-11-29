package com.projectcitybuild.pcbridge.paper.features.groups.domain

import com.projectcitybuild.pcbridge.http.pcb.models.Group
import com.projectcitybuild.pcbridge.paper.features.groups.domain.data.RoleType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage

class ChatGroupFormatter(
    private val rolesFilter: RolesFilter,
) {
    fun format(groups: Set<Group>): Component? {
        val roles = rolesFilter.filter(groups)
        if (roles.isEmpty()) {
            return null
        }
        return Component.text().run {
            roles[RoleType.DONOR]?.let { append(it::component) }
            roles[RoleType.STAFF]?.let { append(it::component) }
            roles[RoleType.TRUST]?.let { append(it::component) }
            roles[RoleType.BUILD]?.let { append(it::component) }
            build()
        }
    }
}

private fun Group.component() = MiniMessage.miniMessage()
    .deserialize(displayName ?: name)
    .also {
        if (hoverText.isNullOrEmpty()) {
            return it
        }
        return it.hoverEvent(
            HoverEvent.showText(Component.text(hoverText!!)),
        )
    }