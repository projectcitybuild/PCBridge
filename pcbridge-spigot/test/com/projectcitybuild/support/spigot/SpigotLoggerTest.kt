package com.projectcitybuild.support.spigot

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import java.util.logging.Logger

class SpigotLoggerTest {
    private lateinit var wrappedLogger: Logger
    private lateinit var logger: SpigotLogger

    @BeforeEach
    fun setUp() {
        wrappedLogger = mock(Logger::class.java)
        logger = SpigotLogger(wrappedLogger)
    }

    @Test
    fun `should log verbose`() {
        val message = "message"
        logger.verbose(message)
        verify(wrappedLogger).info(message)
    }

    @Test
    fun `should log debug`() {
        val message = "message"
        logger.debug(message)
        verify(wrappedLogger).info(message)
    }

    @Test
    fun `should log info`() {
        val message = "message"
        logger.info(message)
        verify(wrappedLogger).info(message)
    }

    @Test
    fun `should log warning`() {
        val message = "message"
        logger.warning(message)
        verify(wrappedLogger).warning(message)
    }

    @Test
    fun `should log severe`() {
        val message = "message"
        logger.severe(message)
        verify(wrappedLogger).severe(message)
    }
}