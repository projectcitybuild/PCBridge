package com.projectcitybuild.modules.errorreporting

interface ErrorReporter {
    fun start()
    fun report(throwable: Throwable)
}
