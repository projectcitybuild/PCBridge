package com.projectcitybuild.libs.errorreporting

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class ErrorReporterTest {

    private lateinit var output: ErrorOutput

    @BeforeEach
    fun setUp() {
        output = mock(ErrorOutput::class.java)
    }

    @Test
    fun `calls start() of registered outputs`() {
        val errorReporter = ErrorReporter(outputs = listOf(output))
        errorReporter.start()

        verify(output).start()
    }

    @Test
    fun `calls report() of registered outputs`() {
        val throwable = mock(Throwable::class.java)
        val errorReporter = ErrorReporter(outputs = listOf(output))
        errorReporter.report(throwable)

        verify(output).report(throwable)
    }
}
