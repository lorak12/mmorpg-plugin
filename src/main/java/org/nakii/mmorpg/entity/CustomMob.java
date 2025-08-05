package org.nakii.mmorpg.entity;

import org.bukkit.configuration.ConfigurationSection;

// This class is a clean data container for a mob's definition from mobs.yml
public class CustomMob {

    private final String id;
    private final ConfigurationSection config;

    public CustomMob(String id, ConfigurationSection config) {
        this.id = id;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public String getBaseType() {
        return config.getString("type", "ZOMBIE");
    }

    public String getDisguiseType() {
        return config.getString("disguise", getBaseType());
    }

    public String getDisplayName() {
        return config.getString("display_name", "Custom Mob");
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public ConfigurationSection getStatsConfig() {
        return config.getConfigurationSection("stats");
    }

    public ConfigurationSection getEquipmentConfig() {
        return config.getConfigurationSection("equipment");
    }
}