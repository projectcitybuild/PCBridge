package com.projectcitybuild.libs.errorreporting

interface ErrorReporter {
    fun start()
    fun report(throwable: Throwable)
}
