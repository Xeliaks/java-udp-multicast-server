package com.game.multicast.server;

import com.game.multicast.common.GameEvent;
import com.game.multicast.common.GameEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventCodec")
class EventCodecTest {

    @Nested
    @DisplayName("encode and decode")
    class EncodeDecode {
        @Test
        void encodeThenDecode_roundtripsEvent() {
            GameEvent original = new GameEvent(GameEventType.PLAYER_MOVED, "p1", "x=100,y=200");
            byte[] encoded = EventCodec.encode(original);
            GameEvent decoded = EventCodec.decode(encoded, 0, encoded.length);
            assertEquals(original, decoded);
        }

        @Test
        void decode_withOffset_usesCorrectSlice() {
            byte[] prefix = "ab".getBytes();
            GameEvent event = new GameEvent(GameEventType.PLAYER_JOINED, "p1", "");
            byte[] encoded = EventCodec.encode(event);
            byte[] withOffset = new byte[prefix.length + encoded.length];
            System.arraycopy(prefix, 0, withOffset, 0, prefix.length);
            System.arraycopy(encoded, 0, withOffset, prefix.length, encoded.length);
            GameEvent decoded = EventCodec.decode(withOffset, prefix.length, encoded.length);
            assertEquals(event, decoded);
        }

        @Test
        void encode_emptyPayload_producesValidJson() {
            GameEvent event = new GameEvent(GameEventType.SCORE_UPDATED, "p1", "");
            byte[] encoded = EventCodec.encode(event);
            assertNotNull(encoded);
            assertTrue(encoded.length > 0);
            GameEvent decoded = EventCodec.decode(encoded, 0, encoded.length);
            assertEquals("", decoded.getPayload());
        }
    }

    @Nested
    @DisplayName("decode invalid data")
    class DecodeInvalid {
        @Test
        void decode_invalidJson_throwsIllegalArgumentException() {
            byte[] invalid = "not json".getBytes();
            assertThrows(IllegalArgumentException.class,
                    () -> EventCodec.decode(invalid, 0, invalid.length));
        }

        @Test
        void decode_emptyArray_throws() {
            byte[] empty = new byte[0];
            assertThrows(Exception.class, () -> EventCodec.decode(empty, 0, 0));
        }
    }

    @Nested
    @DisplayName("encode null handling")
    class EncodeNull {
        @Test
        void encode_nullType_serializesNull() {
            GameEvent event = new GameEvent();
            event.setPlayerId("p1");
            byte[] encoded = EventCodec.encode(event);
            assertNotNull(encoded);
            GameEvent decoded = EventCodec.decode(encoded, 0, encoded.length);
            assertNull(decoded.getType());
            assertEquals("p1", decoded.getPlayerId());
        }
    }
}
