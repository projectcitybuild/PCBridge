// package com.projectcitybuild.modules.chat.commands
//
// import com.projectcitybuild.repositories.PlayerConfigRepository
// import com.projectcitybuild.support.commandapi.ToggleOption
// import com.projectcitybuild.support.textcomponent.send
// import org.bukkit.entity.Player
//
// class BadgeCommand(
//     private val playerConfigRepository: PlayerConfigRepository,
// ) {
//     fun execute(commandSender: Player, desiredState: ToggleOption) {
//         val playerConfig = playerConfigRepository.get(commandSender.uniqueId)!!
//
//         val shouldHideBadge = if (desiredState == ToggleOption.UNSPECIFIED) {
//             !playerConfig.isChatBadgeDisabled
//         } else {
//             desiredState == ToggleOption.OFF
//         }
//         playerConfig.isChatBadgeDisabled = shouldHideBadge
//         playerConfigRepository.save(playerConfig)
//
//         if (shouldHideBadge) {
//             commandSender.send().success("Your chat badge has been turned off")
//         } else {
//             commandSender.send().success("Your chat badge has been turned on")
//         }
//     }
// }
