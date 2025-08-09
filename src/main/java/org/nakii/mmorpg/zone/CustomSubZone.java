package org.nakii.mmorpg.zone;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class CustomSubZone extends CustomZone {
    private final CustomZone parent;

    public CustomSubZone(String id, ConfigurationSection config, CustomZone parent) {
        super(id, config);
        this.parent = parent;
    }

    // Override getters to fall back to the parent if a setting is not defined in the sub-zone
    @Override
    public EnvironmentSettings getEnvironment() {
        return (super.environment != null) ? super.environment : parent.getEnvironment();
    }

    @Override
    public List<MobSpawnInfo> getMobs() {
        return (!super.mobs.isEmpty()) ? super.mobs : parent.getMobs();
    }
}