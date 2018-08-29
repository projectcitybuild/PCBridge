package com.projectcitybuild.core.contracts

interface Injectable {
    var environment: Environment?

    fun inject(environment: Environment) {
        this.environment = environment
    }
}