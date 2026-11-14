package com.taran.app.domain.proxy

import com.taran.app.data.config.Socks5Config
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

class Socks5ServerTest {
    @get:Rule
    val temp = TemporaryFolder()

    private fun withServer(
        cfg: Socks5Config = Socks5Config(),
        protectUdp: Boolean = true,
        block: (Socket, AtomicInteger) -> Unit,
    ) {
        val udpProtectCalls = AtomicInteger()
        val log = ProxyLog(temp.root)
        val server = Socks5Server(
            protect = { true },
            log = log,
            config = cfg.copy(port = 0),
            protectUdp = { udpProtectCalls.incrementAndGet(); protectUdp },
        )
        server.start()
        try {
            // Порт 0 исключает гонку с предварительным поиском свободного порта.
            // Доступ нужен только тесту; производственный API оригинала не меняем.
            val field = Socks5Server::class.java.getDeclaredField("serverSocket")
                .apply { isAccessible = true }
            val listener = field.get(server) as ServerSocket
            Socket("127.0.0.1", listener.localPort).use { socket ->
                socket.soTimeout = 3_000
                block(socket, udpProtectCalls)
            }
        } finally {
            server.stop()
            runBlocking { log.export(temp.newFile()) }
        }
    }

    private fun Socket.send(vararg bytes: Int) {
        getOutputStream().write(bytes.map { it.toByte() }.toByteArray())
        getOutputStream().flush()
    }

    private fun Socket.reply(size: Int): ByteArray {
        val bytes = getInputStream().readNBytes(size)
        assertEquals("truncated SOCKS5 reply", size, bytes.size)
        return bytes
    }

    @Test
    fun defaultNegotiationKeepsNoAuthentication() = withServer { socket, _ ->
        socket.send(5, 1, 0)
        assertArrayEquals(byteArrayOf(5, 0), socket.reply(2))
    }

    @Test
    fun passwordModeAcceptsOnlyCorrectCredentials() {
        for (password in listOf("secret", "wrong")) {
            withServer(Socks5Config(user = "user", pass = "secret")) { socket, _ ->
                socket.send(5, 2, 0, 2)
                assertArrayEquals(byteArrayOf(5, 2), socket.reply(2))
                val credentials = byteArrayOf(1, 4) + "user".toByteArray() +
                    byteArrayOf(password.length.toByte()) + password.toByteArray()
                socket.getOutputStream().write(credentials)
                val expected = if (password == "secret") 0 else 1
                assertArrayEquals(byteArrayOf(1, expected.toByte()), socket.reply(2))
            }
        }
    }

    @Test
    fun passwordModeRejectsClientsOfferingOnlyNoAuthentication() =
        withServer(Socks5Config(user = "user", pass = "secret")) { socket, _ ->
            socket.send(5, 1, 0)
            assertArrayEquals(byteArrayOf(5, 0xFF.toByte()), socket.reply(2))
        }

    @Test
    fun udpAssociateIsDisabledByDefault() = withServer { socket, calls ->
        socket.send(5, 1, 0)
        socket.reply(2)
        socket.send(5, 3, 0, 1, 0, 0, 0, 0, 0, 0)
        assertEquals(7, socket.reply(10)[1].toInt())
        assertEquals(0, calls.get())
    }

    @Test
    fun enabledUdpAssociationProtectsClientRelay() =
        withServer(Socks5Config(udp = true)) { socket, calls ->
            socket.send(5, 1, 0)
            socket.reply(2)
            socket.send(5, 3, 0, 1, 0, 0, 0, 0, 0, 0)
            val reply = socket.reply(10)
            assertEquals(5, reply[0].toInt())
            assertEquals(0, reply[1].toInt())
            assertEquals(1, calls.get())
            val port = ((reply[8].toInt() and 255) shl 8) or (reply[9].toInt() and 255)
            assertTrue(port > 0)
        }

    @Test
    fun udpProtectFailureRejectsAssociation() =
        withServer(Socks5Config(udp = true), protectUdp = false) { socket, calls ->
            socket.send(5, 1, 0)
            socket.reply(2)
            socket.send(5, 3, 0, 1, 0, 0, 0, 0, 0, 0)
            assertEquals(1, socket.reply(10)[1].toInt())
            assertEquals(1, calls.get())
        }
}
