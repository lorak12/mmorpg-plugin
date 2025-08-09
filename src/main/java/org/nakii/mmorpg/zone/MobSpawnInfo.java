package org.nakii.mmorpg.zone;

import org.bukkit.configuration.ConfigurationSection;

public class MobSpawnInfo {
    public final String mobId;
    public final double spawnChance;
    public final int maxInZone;

    public MobSpawnInfo(ConfigurationSection config) {
        this.mobId = config.getString("id");
        this.spawnChance = config.getDouble("spawn_chance");
        this.maxInZone = config.getInt("max_in_zone");
    }
}