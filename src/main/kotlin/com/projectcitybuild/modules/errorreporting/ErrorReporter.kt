package com.projectcitybuild.modules.errorreporting

interface ErrorReporter {
    fun bootstrap()
    fun report(throwable: Throwable)
}