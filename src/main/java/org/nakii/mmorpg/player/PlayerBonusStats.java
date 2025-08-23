package org.nakii.mmorpg.player;

import java.util.EnumMap;
import java.util.Map;

/**
 * A data object to hold permanent stat bonuses for a player,
 * granted from sources like Collection or Slayer rewards.
 */
public class PlayerBonusStats {

    private final Map<Stat, Double> bonusStats;

    public PlayerBonusStats() {
        this.bonusStats = new EnumMap<>(Stat.class);
    }

    public double getBonus(Stat stat) {
        return bonusStats.getOrDefault(stat, 0.0);
    }

    public void setBonus(Stat stat, double value) {
        bonusStats.put(stat, value);
    }

    public void addBonus(Stat stat, double amount) {
        setBonus(stat, getBonus(stat) + amount);
    }

    public Map<Stat, Double> getBonusStatsMap() {
        return bonusStats;
    }
}