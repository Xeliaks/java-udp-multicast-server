package com.game.multicast.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameEvent")
class GameEventTest {

    @Nested
    @DisplayName("construction and getters")
    class ConstructionAndGetters {
        @Test
        void noArgConstructor_setsNullFields() {
            GameEvent event = new GameEvent();
            assertNull(event.getType());
            assertNull(event.getPlayerId());
            assertNull(event.getPayload());
        }

        @Test
        void fullConstructor_setsAllFields() {
            GameEvent event = new GameEvent(GameEventType.PLAYER_MOVED, "p1", "x=10,y=20");
            assertEquals(GameEventType.PLAYER_MOVED, event.getType());
            assertEquals("p1", event.getPlayerId());
            assertEquals("x=10,y=20", event.getPayload());
        }

        @Test
        void setters_updateFields() {
            GameEvent event = new GameEvent();
            event.setType(GameEventType.PLAYER_FIRED);
            event.setPlayerId("p2");
            event.setPayload("targetId=p1");
            assertEquals(GameEventType.PLAYER_FIRED, event.getType());
            assertEquals("p2", event.getPlayerId());
            assertEquals("targetId=p1", event.getPayload());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {
        @Test
        void equals_sameFields_returnsTrue() {
            GameEvent a = new GameEvent(GameEventType.PLAYER_JOINED, "p1", "");
            GameEvent b = new GameEvent(GameEventType.PLAYER_JOINED, "p1", "");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void equals_differentType_returnsFalse() {
            GameEvent a = new GameEvent(GameEventType.PLAYER_JOINED, "p1", "");
            GameEvent b = new GameEvent(GameEventType.PLAYER_LEFT, "p1", "");
            assertNotEquals(a, b);
        }

        @Test
        void equals_differentPlayerId_returnsFalse() {
            GameEvent a = new GameEvent(GameEventType.PLAYER_MOVED, "p1", "x=0");
            GameEvent b = new GameEvent(GameEventType.PLAYER_MOVED, "p2", "x=0");
            assertNotEquals(a, b);
        }

        @Test
        void equals_null_returnsFalse() {
            GameEvent event = new GameEvent(GameEventType.SCORE_UPDATED, "p1", "100");
            assertNotEquals(event, null);
            assertFalse(event.equals(null));
        }

        @Test
        void equals_sameInstance_returnsTrue() {
            GameEvent event = new GameEvent(GameEventType.PLAYER_HIT, "p1", "targetId=p2");
            assertEquals(event, event);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {
        @Test
        void toString_includesTypePlayerIdAndPayload() {
            GameEvent event = new GameEvent(GameEventType.PLAYER_HIT, "p1", "targetId=p2,damage=10");
            String s = event.toString();
            assertTrue(s.contains("PLAYER_HIT"));
            assertTrue(s.contains("p1"));
            assertTrue(s.contains("targetId=p2,damage=10"));
        }
    }
}
