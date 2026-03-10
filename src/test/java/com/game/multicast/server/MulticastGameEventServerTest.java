package com.game.multicast.server;

import com.game.multicast.common.GameEvent;
import com.game.multicast.common.GameEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MulticastGameEventServer")
class MulticastGameEventServerTest {

    private static final String TEST_GROUP = "230.0.0.2";
    private static final int TEST_PORT = 16790;

    private MulticastGameEventServer server;
    private List<GameEvent> receivedEvents;

    @BeforeEach
    void setUp() {
        receivedEvents = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    @Test
    @DisplayName("start joins group and isRunning returns true")
    void start_joinsGroupAndIsRunning() throws Exception {
        server = new MulticastGameEventServer(TEST_GROUP, TEST_PORT, MulticastConfig.MAX_PACKET_SIZE);
        server.start();
        assertTrue(server.isRunning());
    }

    @Test
    @DisplayName("stop leaves group and isRunning returns false")
    void stop_leavesGroupAndIsRunningFalse() throws Exception {
        server = new MulticastGameEventServer(TEST_GROUP, TEST_PORT + 1, MulticastConfig.MAX_PACKET_SIZE);
        server.start();
        assertTrue(server.isRunning());
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test
    @DisplayName("sendEvent throws when server not started")
    void sendEvent_whenNotStarted_throws() {
        server = new MulticastGameEventServer(TEST_GROUP, TEST_PORT + 2, MulticastConfig.MAX_PACKET_SIZE);
        GameEvent event = new GameEvent(GameEventType.PLAYER_JOINED, "p1", "");
        assertThrows(IllegalStateException.class, () -> server.sendEvent(event));
    }

    @Test
    @DisplayName("receives event sent from another socket")
    void receivesEventSentFromAnotherSocket() throws Exception {
        CountDownLatch received = new CountDownLatch(1);
        server = new MulticastGameEventServer(TEST_GROUP, TEST_PORT + 3, MulticastConfig.MAX_PACKET_SIZE, event -> {
            receivedEvents.add(event);
            received.countDown();
        });
        server.start();

        // Send event from "client" socket
        try (MulticastSocket clientSocket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(TEST_GROUP);
            GameEvent sent = new GameEvent(GameEventType.PLAYER_MOVED, "p1", "x=50,y=100");
            byte[] payload = EventCodec.encode(sent);
            DatagramPacket packet = new DatagramPacket(payload, payload.length, group, TEST_PORT + 3);
            clientSocket.send(packet);
        }

        assertTrue(received.await(3, TimeUnit.SECONDS), "Server should receive event within 3s");
        assertEquals(1, receivedEvents.size());
        GameEvent receivedEvent = receivedEvents.get(0);
        assertEquals(GameEventType.PLAYER_MOVED, receivedEvent.getType());
        assertEquals("p1", receivedEvent.getPlayerId());
        assertEquals("x=50,y=100", receivedEvent.getPayload());
    }

    @Test
    @DisplayName("sendEvent encodes and sends; oversize payload throws")
    void sendEvent_oversizePayload_throws() throws Exception {
        server = new MulticastGameEventServer(TEST_GROUP, TEST_PORT + 4, 64);
        server.start();
        StringBuilder big = new StringBuilder();
        while (big.length() < 100) {
            big.append("x=1,y=2,");
        }
        GameEvent event = new GameEvent(GameEventType.PLAYER_MOVED, "p1", big.toString());
        assertThrows(IllegalArgumentException.class, () -> server.sendEvent(event));
    }
}
