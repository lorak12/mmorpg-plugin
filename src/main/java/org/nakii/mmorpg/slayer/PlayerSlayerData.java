package org.nakii.mmorpg.slayer;

import java.util.HashMap;
import java.util.Map;

/**
 * A data object holding a player's XP and level for all slayer types.
 */
public class PlayerSlayerData {

    private final Map<String, Integer> slayerXp;
    private final Map<String, Integer> slayerLevels;
    private final Map<String, Integer> highestTierDefeated;

    public PlayerSlayerData() {
        this.slayerXp = new HashMap<>();
        this.slayerLevels = new HashMap<>();
        this.highestTierDefeated = new HashMap<>();
    }

    public int getXp(String slayerType) {
        return slayerXp.getOrDefault(slayerType.toUpperCase(), 0);
    }

    public void setXp(String slayerType, int amount) {
        slayerXp.put(slayerType.toUpperCase(), amount);
    }

    public void addXp(String slayerType, int amount) {
        setXp(slayerType, getXp(slayerType) + amount);
    }

    public int getLevel(String slayerType) {
        return slayerLevels.getOrDefault(slayerType.toUpperCase(), 0);
    }

    public void setLevel(String slayerType, int level) {
        slayerLevels.put(slayerType.toUpperCase(), level);
    }

    public Map<String, Integer> getSlayerXpMap() {
        return slayerXp;
    }

    public Map<String, Integer> getSlayerLevelsMap() {
        return slayerLevels;
    }

    public int getHighestTierDefeated(String slayerType) {
        return highestTierDefeated.getOrDefault(slayerType.toUpperCase(), 0);
    }

    public void setHighestTierDefeated(String slayerType, int tier) {
        if (tier > getHighestTierDefeated(slayerType)) {
            highestTierDefeated.put(slayerType.toUpperCase(), tier);
        }
    }

    // This will be needed for saving to the database
    public Map<String, Integer> getHighestTierDefeatedMap() {
        return highestTierDefeated;
    }
}