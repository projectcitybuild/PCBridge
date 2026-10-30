package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class HealCommandTest {
    @Test
    fun `accepts a target player argument`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = HealCommand(harness.plugin, harness.server, harness.executor)

        harness.assertParses(command, "heal Alex")
    }
}
