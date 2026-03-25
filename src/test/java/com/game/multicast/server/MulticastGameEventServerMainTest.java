package com.game.multicast.server;

import com.game.multicast.common.GameEvent;
import com.game.multicast.common.GameEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MulticastGameEventServerMain Parser")
class MulticastGameEventServerMainTest {

    @Test
    @DisplayName("Valid input with payload is parsed correctly")
    void parse_validInputWithPayload() {
        GameEvent event = MulticastGameEventServerMain.parseServerCommand("GAME_STARTED system map=arena1");
        assertNotNull(event);
        assertEquals(GameEventType.GAME_STARTED, event.getType());
        assertEquals("system", event.getPlayerId());
        assertEquals("map=arena1", event.getPayload());
    }

    @Test
    @DisplayName("Valid input without payload is parsed correctly")
    void parse_validInputWithoutPayload() {
        GameEvent event = MulticastGameEventServerMain.parseServerCommand("GAME_PAUSED system");
        assertNotNull(event);
        assertEquals(GameEventType.GAME_PAUSED, event.getType());
        assertEquals("system", event.getPlayerId());
        assertEquals("", event.getPayload());
    }

    @Test
    @DisplayName("Missing player ID throws IllegalArgumentException")
    void parse_missingPlayerId_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MulticastGameEventServerMain.parseServerCommand("GAME_PAUSED")
        );
        assertTrue(ex.getMessage().contains("Missing player ID or event type"));
    }

    @Test
    @DisplayName("Unknown event type throws IllegalArgumentException")
    void parse_unknownEventType_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MulticastGameEventServerMain.parseServerCommand("INVALID_EVENT p1 something")
        );
        assertTrue(ex.getMessage().contains("Unknown event type 'INVALID_EVENT'"));
    }

    @Test
    @DisplayName("Extra spaces are handled correctly")
    void parse_extraSpaces_handledCorrectly() {
        GameEvent event = MulticastGameEventServerMain.parseServerCommand("  SCORE_UPDATED   p1   100  ");
        assertNotNull(event);
        assertEquals(GameEventType.SCORE_UPDATED, event.getType());
        assertEquals("p1", event.getPlayerId());
        assertEquals("100", event.getPayload());
    }
}