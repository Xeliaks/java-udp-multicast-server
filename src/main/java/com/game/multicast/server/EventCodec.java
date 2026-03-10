package com.game.multicast.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.multicast.common.GameEvent;

/**
 * Serializes and deserializes {@link GameEvent} to/from JSON for UDP payloads.
 */
public final class EventCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static byte[] encode(GameEvent event) {
        try {
            return MAPPER.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to encode event: " + event, e);
        }
    }

    public static GameEvent decode(byte[] data, int offset, int length) {
        try {
            return MAPPER.readValue(data, offset, length, GameEvent.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode event from " + length + " bytes", e);
        }
    }

    private EventCodec() {}
}
