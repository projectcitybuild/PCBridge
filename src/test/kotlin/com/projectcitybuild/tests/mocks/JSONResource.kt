package com.projectcitybuild.tests.mocks

import okhttp3.mockwebserver.MockResponse
import java.io.InputStreamReader

private class JSONResource {

    companion object {
        fun <T: Any>find(fileName: String, test: T): String {
            val resource = test.javaClass.classLoader.getResourceAsStream(fileName)
                    ?: throw Exception("Cannot find json file: $fileName")

            val reader = InputStreamReader(resource)
            val content = reader.readText()
            reader.close()
            return content
        }
    }
}

fun MockResponse.withJSONResource(fileName: String): MockResponse {
    val json = JSONResource.find(fileName, this)

    return this
            .setBody(json)
            .setHeader("Content-Type", "application/json")
}