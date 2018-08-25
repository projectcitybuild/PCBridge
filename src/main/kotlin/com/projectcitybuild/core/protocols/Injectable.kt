package com.projectcitybuild.core.protocols

interface Injectable {
    var environment: Environment

    fun inject(environment: Environment) {
        this.environment = environment
    }
}