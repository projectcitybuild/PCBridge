package com.projectcitybuild.libs.errorreporting

interface ErrorOutput {
    fun start()
    fun report(throwable: Throwable)
}