// package com.projectcitybuild.modules.telemetry
//
// import com.projectcitybuild.modules.telemetry.listeners.PlayerJoinListener
// import com.projectcitybuild.modules.telemetry.listeners.PlayerQuitListener
// import com.projectcitybuild.support.modules.ModuleDeclaration
// import com.projectcitybuild.support.modules.PluginModule
//
// class TelemetryModule: PluginModule {
//     override fun register(module: ModuleDeclaration) {
//         module {
//             listener(
//                 PlayerJoinListener(container.telemetryRepository),
//             )
//             listener(
//                 PlayerQuitListener(container.telemetryRepository),
//             )
//         }
//     }
// }