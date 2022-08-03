package com.projectcitybuild.plugin.assembly.providers

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.adapters.YamlKeyValueStorage
import dagger.Module
import dagger.Provides

@Module
class ConfigProvider {

    @Provides
    fun providesConfig(
        yamlKeyValueStorage: YamlKeyValueStorage,
    ): Config {
        return Config(yamlKeyValueStorage)
    }
}
