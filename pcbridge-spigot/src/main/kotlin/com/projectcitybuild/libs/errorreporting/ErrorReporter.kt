package com.projectcitybuild.libs.errorreporting

class ErrorReporter(
    private val outputs: List<ErrorOutput>,
) {
    fun start() {
        outputs.forEach { it.start() }
    }

    fun report(throwable: Throwable) {
        outputs.forEach { it.report(throwable) }
    }
}
