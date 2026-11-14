package com.taran.app.domain.proxy

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.InetAddress

class Socks5UdpTest {
    @Test
    fun decodesIpv4AndIpv6Payloads() {
        val payload = byteArrayOf(10, 20, 30)
        for (ip in listOf("192.0.2.1", "2001:db8::1")) {
            val address = InetAddress.getByName(ip)
            val packet = Socks5Udp.header(address, 53001) + payload
            val parsed = requireNotNull(Socks5Udp.parse(packet, packet.size))
            assertEquals(address, InetAddress.getByName(parsed.host))
            assertEquals(53001, parsed.port)
            assertArrayEquals(payload, packet.copyOfRange(parsed.dataOffset, packet.size))
        }
    }

    @Test
    fun decodesDomainNameAndPort() {
        val name = "example.test".toByteArray(Charsets.US_ASCII)
        val packet = byteArrayOf(0, 0, 0, 3, name.size.toByte()) + name + byteArrayOf(0, 53, 42)
        val parsed = requireNotNull(Socks5Udp.parse(packet, packet.size))
        assertEquals("example.test", parsed.host)
        assertEquals(53, parsed.port)
        assertEquals(42, packet[parsed.dataOffset].toInt())
    }

    @Test
    fun rejectsFragmentsUnknownAddressTypesAndTruncatedPackets() {
        val valid = Socks5Udp.header(InetAddress.getByName("192.0.2.1"), 53)
        for (length in 0 until valid.size) {
            assertNull(Socks5Udp.parse(valid, length))
        }
        assertNull(Socks5Udp.parse(valid.copyOf().apply { this[2] = 1 }, valid.size))
        assertNull(Socks5Udp.parse(valid.copyOf().apply { this[3] = 2 }, valid.size))
        assertNull(Socks5Udp.parse(byteArrayOf(0, 0, 0, 3, 0), 5))
        assertNull(Socks5Udp.parse(byteArrayOf(0, 0, 0, 3, 5, 65), 6))
    }
}
