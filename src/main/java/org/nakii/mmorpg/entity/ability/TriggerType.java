package org.nakii.mmorpg.entity.ability;

public enum TriggerType {
    ON_ATTACK,    // When this mob damages something
    ON_DAMAGED,   // When this mob is damaged
    TIMER,        // Periodically
    ON_DEATH      // When this mob dies
}