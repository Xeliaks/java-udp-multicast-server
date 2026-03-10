package com.game.multicast.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MulticastConfig")
class MulticastConfigTest {

    @Test
    void getMulticastAddress_returnsValidInetAddress() {
        InetAddress addr = MulticastConfig.getMulticastAddress();
        assertNotNull(addr);
        assertEquals(MulticastConfig.MULTICAST_GROUP, addr.getHostAddress());
    }

    @Test
    void multicastGroup_inValidRange() {
        String group = MulticastConfig.MULTICAST_GROUP;
        assertTrue(group.startsWith("22") || group.startsWith("23"),
                "Multicast IPv4 range is 224.0.0.0 - 239.255.255.255");
    }

    @Test
    void port_isPositive() {
        assertTrue(MulticastConfig.MULTICAST_PORT > 0 && MulticastConfig.MULTICAST_PORT < 65536);
    }

    @Test
    void maxPacketSize_isPositive() {
        assertTrue(MulticastConfig.MAX_PACKET_SIZE > 0);
    }
}
