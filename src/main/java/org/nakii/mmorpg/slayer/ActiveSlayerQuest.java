package org.nakii.mmorpg.slayer;

/**
 * A simple data object to hold information about a player's active slayer quest.
 */
public class ActiveSlayerQuest {
    private final String slayerType;
    private final int tier;
    private final int xpToSpawn;
    private double currentXp;

    public ActiveSlayerQuest(String slayerType, int tier, int xpToSpawn) {
        this.slayerType = slayerType;
        this.tier = tier;
        this.xpToSpawn = xpToSpawn;
        this.currentXp = 0;
    }

    public String getSlayerType() { return slayerType; }
    public int getTier() { return tier; }
    public int getXpToSpawn() { return xpToSpawn; }
    public double getCurrentXp() { return currentXp; }

    public void addXp(double amount) {
        this.currentXp += amount;
    }

    public boolean isComplete() {
        return this.currentXp >= this.xpToSpawn;
    }
}