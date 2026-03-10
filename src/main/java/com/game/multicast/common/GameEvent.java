package com.game.multicast.common;

import java.util.Objects;

/**
 * Game-state event exchanged over the multicast group.
 * All clients subscribed to the group receive the same events.
 */
public class GameEvent {

    private GameEventType type;
    private String playerId;
    private String payload;  // JSON or key=value for type-specific data (e.g. "x=100,y=200" or "targetId=p2,damage=10")

    public GameEvent() {}

    public GameEvent(GameEventType type, String playerId, String payload) {
        this.type = type;
        this.playerId = playerId;
        this.payload = payload;
    }

    public GameEventType getType() {
        return type;
    }

    public void setType(GameEventType type) {
        this.type = type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameEvent event = (GameEvent) o;
        return type == event.type
                && Objects.equals(playerId, event.playerId)
                && Objects.equals(payload, event.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, playerId, payload);
    }

    @Override
    public String toString() {
        return "GameEvent{type=" + type + ", playerId='" + playerId + "', payload='" + payload + "'}";
    }
}
