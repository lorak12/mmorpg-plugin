package org.nakii.mmorpg.zone;

public record PlayerVisualFlags(
        Long time, // Use Long for player time
        String weather
) {}
