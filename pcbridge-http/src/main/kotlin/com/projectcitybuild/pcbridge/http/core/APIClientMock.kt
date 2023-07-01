package com.projectcitybuild.pcbridge.http.core

class APIClientMock : APIClient {

    var result: Any? = null
    var exception: Throwable? = null

    override suspend fun <T> execute(apiCall: suspend () -> T): T {
        val exception = exception
        if (exception != null) {
            throw exception
        }
        if (result == null) {
            throw Exception("No result mocked")
        }
        return result as T
    }
}
