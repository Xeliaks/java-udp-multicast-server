package com.game.multicast.common;

/**
 * Event types for the multicast game event bus (arena/battle simulation).
 */
public enum GameEventType {
    PLAYER_JOINED,
    PLAYER_MOVED,
    PLAYER_FIRED,
    PLAYER_HIT,
    PLAYER_LEFT,
    SCORE_UPDATED
}
