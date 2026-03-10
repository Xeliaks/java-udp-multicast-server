package com.game.multicast.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameEventType")
class GameEventTypeTest {

    @Test
    void allArenaEventTypesPresent() {
        GameEventType[] values = GameEventType.values();
        assertEquals(6, values.length);
        assertNotNull(GameEventType.valueOf("PLAYER_JOINED"));
        assertNotNull(GameEventType.valueOf("PLAYER_MOVED"));
        assertNotNull(GameEventType.valueOf("PLAYER_FIRED"));
        assertNotNull(GameEventType.valueOf("PLAYER_HIT"));
        assertNotNull(GameEventType.valueOf("PLAYER_LEFT"));
        assertNotNull(GameEventType.valueOf("SCORE_UPDATED"));
    }
}
