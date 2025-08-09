package org.nakii.mmorpg.zone;

import org.bukkit.configuration.ConfigurationSection;

public class EnvironmentSettings {
    public final String type;
    public final double increasePerSecond;
    public final double maxValue;
    public final double damageThreshold;
    public final double damageAmount;

    public EnvironmentSettings(ConfigurationSection config) {
        this.type = config.getString("type", "natural");
        this.increasePerSecond = config.getDouble("increase_per_second", 5.0);
        this.maxValue = config.getDouble("max_value", 100.0);
        this.damageThreshold = config.getDouble("damage_threshold", 90.0);
        this.damageAmount = config.getDouble("damage_amount", 5.0);
    }
}