package org.nakii.mmorpg.stats;

/**
 * A structured container for breaking down a player's stats by their source.
 * Instead of having hundreds of fields, it holds PlayerStats objects for each source.
 */
public class StatBreakdown {

    private final PlayerStats baseStats;
    private final PlayerStats itemStats;
    private final PlayerStats skillStats;
    private final PlayerStats totalStats;

    public StatBreakdown(PlayerStats baseStats, PlayerStats itemStats, PlayerStats skillStats, PlayerStats totalStats) {
        this.baseStats = baseStats;
        this.itemStats = itemStats;
        this.skillStats = skillStats;
        this.totalStats = totalStats;
    }

    public PlayerStats getBaseStats() {
        return baseStats;
    }

    public PlayerStats getItemStats() {
        return itemStats;
    }

    public PlayerStats getSkillStats() {
        return skillStats;
    }

    public PlayerStats getTotalStats() {
        return totalStats;
    }
}