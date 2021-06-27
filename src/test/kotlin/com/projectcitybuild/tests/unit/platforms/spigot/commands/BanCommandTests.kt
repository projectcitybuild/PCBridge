package com.projectcitybuild.tests.unit.platforms.spigot.commands

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.projectcitybuild.core.contracts.CommandResult
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.LogLevel
import com.projectcitybuild.core.entities.Player
import com.projectcitybuild.core.entities.PluginConfigPair
import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.core.utilities.AsyncTask
import com.projectcitybuild.core.utilities.Cancellable
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.platforms.spigot.commands.BanCommand
import junit.framework.TestCase.assertEquals
import net.luckperms.api.LuckPerms
import org.bukkit.plugin.java.JavaPlugin
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*

class EnvironmentMock: EnvironmentProvider {

    private val memoryConfig = HashMap<String, Any>()

    override fun get(key: PluginConfigPair) : Any {
        return memoryConfig[key.key] ?: key.defaultValue
    }

    override fun set(key: PluginConfigPair, value: Any) {
        memoryConfig[key.key] = value
    }

    override fun log(level: LogLevel, message: String) {}

    override fun get(player: UUID) : Player? {
        throw NotImplementedError()
    }

    override fun set(player: Player) {
        throw NotImplementedError()
    }

    // Runs a given unit of work on a background thread asynchronously
    override fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T> {
        return AsyncTask { resolve ->
            task { result -> resolve(result) }
            Cancellable {}
        }
    }

    // Runs a given unit of work on the main thread synchronously
    override fun sync(task: () -> Unit) {
        task()
    }

    override val permissions: LuckPerms?
        get() = throw NotImplementedError()

    override val plugin: JavaPlugin?
        get() = throw NotImplementedError()
}

class BanCommandTests {

    private val mockPCBClient: PCBClient
        get() = Mockito.mock(PCBClient::class.java)

    private val mockMojangClient: MojangClient
        get() = Mockito.mock(MojangClient::class.java)

    private val mockCreateBanAction: CreateBanAction
        get() = Mockito.mock(CreateBanAction::class.java)

    private lateinit var server: ServerMock

    @Before
    fun setUp() {
        server = MockBukkit.mock()
    }

    @After
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `shows successful ban to user`() {
        val banTargetPlayer = server.addPlayer()
        val staffPlayer = Mockito.spy(server.addPlayer())
        val banReason = "fake_reason"

        val createBanAction = mockCreateBanAction
        Mockito.`when`(createBanAction.execute(
                banTargetPlayer.uniqueId,
                banTargetPlayer.displayName,
                staffPlayer.uniqueId,
                banReason
        )).thenReturn(CreateBanAction.Result.SUCCESS())

        val command = BanCommand(
                environment = EnvironmentMock(),
                networkClients = NetworkClients(mockPCBClient, mockMojangClient),
                createBanAction = createBanAction
        )
        val input = CommandInput(
                sender = staffPlayer,
                args = arrayOf(banTargetPlayer.displayName, banReason),
                isConsole = false
        )
        val result = command.execute(input)

        assertEquals(CommandResult.EXECUTED, result)
    }
}