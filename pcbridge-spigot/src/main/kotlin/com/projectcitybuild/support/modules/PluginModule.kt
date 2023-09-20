package com.projectcitybuild.support.modules

@Deprecated("Inherit PluginFeature instead")
interface PluginModule {
    fun register(module: ModuleDeclaration)
    fun unregister() {}
}