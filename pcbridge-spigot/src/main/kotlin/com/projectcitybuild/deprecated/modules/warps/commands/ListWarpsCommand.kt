// package com.projectcitybuild.modules.warps.commands
//
// import com.projectcitybuild.modules.warps.actions.GetWarpList
// import com.projectcitybuild.support.textcomponent.add
// import com.projectcitybuild.support.textcomponent.send
// import net.md_5.bungee.api.ChatColor
// import net.md_5.bungee.api.chat.ClickEvent
// import net.md_5.bungee.api.chat.HoverEvent
// import net.md_5.bungee.api.chat.TextComponent
// import net.md_5.bungee.api.chat.hover.content.Text
// import org.bukkit.entity.Player
// import java.lang.Integer.max
//
// class ListWarpsCommand(
//     private val getWarpList: GetWarpList,
// ) {
//     fun execute(commandSender: Player, pageIndex: Int?) {
//         val clampedPageIndex = max(1, pageIndex ?: 1)
//         val warpList = getWarpList.getList(clampedPageIndex)
//         if (warpList == null) {
//             commandSender.send().info("No warps available")
//             return
//         }
//
//         val tc = TextComponent()
//             .add("Warps (${warpList.totalWarps})") { it.isBold = true }
//             .add("\n---\n")
//
//         warpList.warps.withIndex().forEach { (index, name) ->
//             if (index != 0) {
//                 tc.add(", ")
//             }
//             tc.add(
//                 TextComponent(name).also {
//                     it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp $name")
//                     it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("/warp $name"))
//                     it.isUnderlined = true
//                 }
//             )
//         }
//
//         if (warpList.totalPages > 1) {
//             tc.add("\n---\n")
//                 .add("Page $clampedPageIndex of ${warpList.totalPages}") { it.color = ChatColor.GRAY }
//         }
//
//         commandSender.spigot().sendMessage(tc)
//     }
// }
