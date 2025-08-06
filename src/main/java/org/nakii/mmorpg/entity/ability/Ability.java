package org.nakii.mmorpg.entity.ability;

import org.bukkit.configuration.ConfigurationSection;
// Represents a single ability block from mobs.yml
public class Ability {
    private final String id;
    private final TriggerType trigger;
    private final ConfigurationSection config;

    public Ability(String id, ConfigurationSection config) {
        this.id = id;
        this.config = config;
        this.trigger = TriggerType.valueOf(config.getString("trigger", "TIMER").toUpperCase());
    }

    public String getId() { return id; }
    public TriggerType getTrigger() { return trigger; }
    public ConfigurationSection getConfig() { return config; }
    public ConfigurationSection getConditionsConfig() { return config.getConfigurationSection("conditions"); }
}