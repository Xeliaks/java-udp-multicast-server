package com.game.multicast.server;

import com.game.multicast.common.GameEvent;
import com.game.multicast.common.GameEventType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the UDP multicast game event bus server.
 * <p>
 * Starts the server and optionally sends server-originated events from stdin for testing
 * (e.g. "player_joined p1", "score_updated p1 100").
 */
public class MulticastGameEventServerMain {

    private static final Logger LOG = Logger.getLogger(MulticastGameEventServerMain.class.getName());

    public static void main(String[] args) throws IOException {
        String group = args.length > 0 ? args[0] : MulticastConfig.MULTICAST_GROUP;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : MulticastConfig.MULTICAST_PORT;

        MulticastGameEventServer server = new MulticastGameEventServer(
                group,
                port,
                MulticastConfig.MAX_PACKET_SIZE
        );

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "shutdown"));

        server.start();

        boolean interactive = System.console() != null && !"true".equalsIgnoreCase(System.getProperty("server.headless"));

        if (interactive) {
            System.out.println("Enter events (e.g. PLAYER_JOINED p1, SCORE_UPDATED p1 100) or 'quit' to exit.");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    if ("quit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line)) {
                        break;
                    }
                    try {
                        GameEvent event = parseServerCommand(line);
                        if (event != null) {
                            server.sendEvent(event);
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Failed to send event: " + line, e);
                    }
                }
            }
        } else {
            LOG.info("Headless mode: server listening only. Send events from clients.");
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        server.stop();
    }

    /**
     * Simple parser for manual server commands: "type playerId [payload]"
     * Example: "score_updated p1 150"
     */
    private static GameEvent parseServerCommand(String line) {
        String[] parts = line.split("\\s+", 3);
        if (parts.length < 2) return null;
        GameEventType type;
        try {
            type = GameEventType.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            LOG.warning("Unknown event type: " + parts[0]);
            return null;
        }
        String playerId = parts[1];
        String payload = parts.length > 2 ? parts[2] : "";
        return new GameEvent(type, playerId, payload);
    }
}
