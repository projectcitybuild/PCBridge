package com.projectcitybuild.pcbridge.runtime.features

import com.projectcitybuild.pcbridge.runtime.features.PaperFeatureRegistration
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
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
