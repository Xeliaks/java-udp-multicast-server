package com.game.multicast.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Configuration for the multicast game event bus.
 */
public final class MulticastConfig {

    /** Multicast group address (IPv4 multicast range 224.0.0.0 - 239.255.255.255). */
    public static final String MULTICAST_GROUP = "230.0.0.1";

    /** Port used for multicast send/receive. */
    public static final int MULTICAST_PORT = 6789;

    /** Max size of a single event datagram (bytes). */
    public static final int MAX_PACKET_SIZE = 1024;

    public static InetAddress getMulticastAddress() {
        try {
            return InetAddress.getByName(MULTICAST_GROUP);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Invalid multicast address: " + MULTICAST_GROUP, e);
        }
    }

    private MulticastConfig() {}
}
