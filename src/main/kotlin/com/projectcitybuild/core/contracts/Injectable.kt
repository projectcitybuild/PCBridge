package com.projectcitybuild.core.contracts

interface Injectable {
    var environment: EnvironmentProvider?

    fun inject(environment: EnvironmentProvider) {
        this.environment = environment
    }
}