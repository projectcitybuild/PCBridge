package com.projectcitybuild.modules.network

import dagger.Reusable

@Reusable
class APIClientMock: APIClient {

    var result: Any? = null

    override suspend fun <T> execute(apiCall: suspend () -> T): T {
        if (result == null) {
            throw Exception("No result mocked")
        }
        return result as T
    }
}