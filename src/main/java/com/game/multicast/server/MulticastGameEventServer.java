package com.game.multicast.server;

import com.game.multicast.common.GameEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UDP multicast game event bus server.
 * <p>
 * Joins the multicast group and:
 * <ul>
 *   <li>Listens for events from clients (and other peers)</li>
 *   <li>Sends events to the group (server-originated or relay)</li>
 * </ul>
 * All members of the group receive the same events in near real time.
 */
public class MulticastGameEventServer {

    private static final Logger LOG = Logger.getLogger(MulticastGameEventServer.class.getName());

    private final String multicastGroup;
    private final int port;
    private final int maxPacketSize;
    private final Consumer<GameEvent> receivedEventListener;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private MulticastSocket socket;
    private InetAddress groupAddress;
    private Thread receiverThread;

    public MulticastGameEventServer(String multicastGroup, int port, int maxPacketSize) {
        this(multicastGroup, port, maxPacketSize, null);
    }

    /** Constructor with optional listener for received events (e.g. for tests). */
    public MulticastGameEventServer(String multicastGroup, int port, int maxPacketSize, Consumer<GameEvent> receivedEventListener) {
        this.multicastGroup = multicastGroup;
        this.port = port;
        this.maxPacketSize = maxPacketSize;
        this.receivedEventListener = receivedEventListener;
    }

    /**
     * Starts the server: joins the multicast group and begins receiving events.
     */
    public void start() throws IOException {
        if (running.getAndSet(true)) {
            LOG.warning("Server already running");
            return;
        }
        groupAddress = InetAddress.getByName(multicastGroup);
        socket = new MulticastSocket(port);
        socket.joinGroup(groupAddress);
        socket.setSoTimeout(5000); // allow periodic check of running flag

        receiverThread = new Thread(this::receiveLoop, "multicast-receiver");
        receiverThread.setDaemon(false);
        receiverThread.start();

        LOG.info("Multicast game event server started on " + multicastGroup + ":" + port);
    }

    /**
     * Stops the server: leaves the group and closes the socket.
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }
        if (receiverThread != null) {
            receiverThread.interrupt();
            try {
                receiverThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.log(Level.WARNING, "Interrupted while joining receiver thread", e);
            }
        }
        if (socket != null && !socket.isClosed()) {
            try {
                socket.leaveGroup(groupAddress);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Error leaving multicast group", e);
            }
            socket.close();
        }
        LOG.info("Multicast game event server stopped");
    }

    /**
     * Sends a game event to the multicast group. All members (including this server) can receive it.
     */
    public void sendEvent(GameEvent event) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IllegalStateException("Server not started");
        }
        byte[] payload = EventCodec.encode(event);
        if (payload.length > maxPacketSize) {
            throw new IllegalArgumentException("Event payload exceeds max size: " + payload.length + " > " + maxPacketSize);
        }
        DatagramPacket packet = new DatagramPacket(payload, payload.length, groupAddress, port);
        socket.send(packet);
        LOG.fine("Sent event: " + event.getType() + " from " + event.getPlayerId());
    }

    /**
     * Receives events in a loop and dispatches them (log and optional relay/handling).
     */
    private void receiveLoop() {
        byte[] buffer = new byte[maxPacketSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (running.get()) {
            try {
                socket.receive(packet);
                handleReceivedPacket(packet);
            } catch (java.net.SocketTimeoutException e) {
                // expected; allows checking running flag
                continue;
            } catch (IOException e) {
                if (running.get()) {
                    LOG.log(Level.WARNING, "Error receiving packet", e);
                }
            }
        }
    }

    /**
     * Handles one received datagram: decode as GameEvent and log (and optionally relay or apply game logic).
     */
    private void handleReceivedPacket(DatagramPacket packet) {
        int length = packet.getLength();
        if (length == 0) {
            return;
        }
        try {
            GameEvent event = EventCodec.decode(packet.getData(), packet.getOffset(), length);
            LOG.info("Received: " + event);
            if (receivedEventListener != null) {
                receivedEventListener.accept(event);
            }
        } catch (Exception e) {
            String raw = new String(packet.getData(), packet.getOffset(), length, StandardCharsets.UTF_8);
            LOG.log(Level.WARNING, "Failed to parse event (raw): " + raw, e);
        }
    }

    public boolean isRunning() {
        return running.get();
    }
}
