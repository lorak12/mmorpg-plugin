package org.nakii.mmorpg.player;

import java.util.EnumMap;
import java.util.Map;

public class PlayerStats {

    private final Map<Stat, Double> stats = new EnumMap<>(Stat.class);

    public PlayerStats() {
        for (Stat stat : Stat.values()) {
            stats.put(stat, 0.0);
        }
    }

    public double getStat(Stat stat) {
        return stats.getOrDefault(stat, 0.0);
    }

    public void setStat(Stat stat, double value) {
        stats.put(stat, value);
    }

    public void addStat(Stat stat, double amount) {
        stats.put(stat, getStat(stat) + amount);
    }



    // --- CONVENIENCE GETTERS ---
    public double getDamage() { return getStat(Stat.DAMAGE); }
    public double getHealth() { return getStat(Stat.HEALTH); }
    public double getDefense() { return getStat(Stat.DEFENSE); }
    public double getStrength() { return getStat(Stat.STRENGTH); }
    public double getIntelligence() { return getStat(Stat.INTELLIGENCE); }
    public double getCritChance() { return getStat(Stat.CRIT_CHANCE); }
    public double getCritDamage() { return getStat(Stat.CRIT_DAMAGE); }
    public double getSpeed() { return getStat(Stat.SPEED); }
    public double getHealthRegen() { return getStat(Stat.HEALTH_REGEN); }
    public double getHeatResistance() { return getStat(Stat.HEAT_RESISTANCE); }
    public double getColdResistance() { return getStat(Stat.COLD_RESISTANCE); }
}