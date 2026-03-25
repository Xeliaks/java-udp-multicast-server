package com.game.multicast.server;

import com.game.multicast.common.GameEvent;
import com.game.multicast.common.GameEventType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
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
            System.out.println("Enter events (e.g. GAME_STARTED system map=arena1, GAME_PAUSED system, PLAYER_JOINED p1, STATS for statistics) or 'quit' to exit.");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    
                    // Handle empty input
                    if (line.isEmpty()) continue; 
                    
                    if ("quit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line)) {
                        break;
                    }

                    if ("stats".equalsIgnoreCase(line)) {
                        server.printStatistics();
                        continue;
                    }

                    try {
                        GameEvent event = parseServerCommand(line);
                        if (event != null) {
                            server.sendEvent(event);
                        }
                    // Invalid input must produce a clear error message without crashing
                    } catch (IllegalArgumentException e) {
                        LOG.warning("Invalid input: " + e.getMessage());
                    // Handle malformed payloads or unexpected errors during sending
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Unexpected error while processing or sending event: " + line, e);
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
    public static GameEvent parseServerCommand(String line) {
       if (line != null) {
            line = line.trim();
        }
        
        String[] parts = line.split("\\s+", 3);
        
        // Handles missing player ID or event type explicitly
        if (parts.length < 2 || parts[0].isEmpty()) {
            throw new IllegalArgumentException("Missing player ID or event type. Expected format: EVENT_TYPE PLAYER_ID [PAYLOAD]");
        }
        
        GameEventType type;
        try {
            type = GameEventType.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handles unknown types by throwing a detailed error showing valid options
            throw new IllegalArgumentException("Unknown event type '" + parts[0] + "'. Available types: " + Arrays.toString(GameEventType.values()));
        }
        
        String playerId = parts[1];
        String payload = parts.length > 2 ? parts[2] : "";
        
        return new GameEvent(type, playerId, payload);
    }
}
