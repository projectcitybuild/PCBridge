package com.projectcitybuild.pcbridge.core.localconfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.io.File

class LocalConfigTest {
    @Test
    fun `get returns the backing config and reads it only once`() {
        val file = File("local-config.json")
        val config = LocalConfigKeyValues.default()
        val backing = FakeBacking(config)
        val localConfig = LocalConfig(file, backing)

        assertSame(config, localConfig.get())
        assertSame(config, localConfig.get())
        assertEquals(file, backing.readFile)
        assertEquals(1, backing.readCount)
    }

    @Test
    fun `get falls back to defaults when the backing config is missing`() {
        val localConfig = LocalConfig(File("missing.json"), FakeBacking(null))

        assertEquals(LocalConfigKeyValues.default(), localConfig.get())
    }

    private class FakeBacking(
        private val result: LocalConfigKeyValues?,
    ) : LocalConfigBacking<LocalConfigKeyValues> {
        var readFile: File? = null
        var readCount: Int = 0

        override fun readSync(file: File): LocalConfigKeyValues? {
            readFile = file
            readCount++
            return result
        }
    }
}
