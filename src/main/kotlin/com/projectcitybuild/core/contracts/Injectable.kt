package com.projectcitybuild.core.contracts

import com.projectcitybuild.api.APIProvider

interface Injectable {
    var environment: EnvironmentProvider?
    var apiProvider: APIProvider?

    fun inject(environment: EnvironmentProvider, apiProvider: APIProvider) {
        this.environment = environment
        this.apiProvider = apiProvider
    }
}