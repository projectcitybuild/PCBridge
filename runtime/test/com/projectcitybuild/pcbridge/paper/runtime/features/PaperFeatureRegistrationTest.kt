package com.projectcitybuild.pcbridge.paper.runtime.features

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class PaperFeatureRegistrationTest {
    @Test
    fun `feature registrations can coexist in Koin`() {
        val application =
            koinApplication {
                modules(
                    module {
                        paperFeature("first") {}
                        paperFeature("second") {}
                    },
                )
            }

        assertEquals(
            setOf("first", "second"),
            application.koin
                .getAll<PaperFeatureRegistration>()
                .map { it.name }
                .toSet(),
        )

        application.close()
    }
}
