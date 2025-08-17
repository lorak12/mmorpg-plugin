package org.nakii.mmorpg.slayer;

import org.bukkit.entity.LivingEntity;

/**
 * A simple data object to hold information about a player's active slayer quest.
 */
public class ActiveSlayerQuest {

    // --- ADD THIS ENUM ---
    public enum QuestState {
        GATHERING_XP,
        BOSS_FIGHT,
        AWAITING_CLAIM
    }

    private QuestState state; // ADD THIS
    private LivingEntity activeBoss; // ADD THIS to track the spawned boss

    private final String slayerType;
    private final int tier;
    private final int xpToSpawn;
    private double currentXp;

    public ActiveSlayerQuest(String slayerType, int tier, int xpToSpawn) {
        this.slayerType = slayerType;
        this.tier = tier;
        this.xpToSpawn = xpToSpawn;
        this.currentXp = 0;
        this.state = QuestState.GATHERING_XP; // Set initial state
    }

    // --- ADD/MODIFY these methods ---
    public QuestState getState() { return state; }
    public void setState(QuestState state) { this.state = state; }

    public LivingEntity getActiveBoss() { return activeBoss; }
    public void setActiveBoss(LivingEntity boss) { this.activeBoss = boss; }

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

    public void setCurrentXp(double currentXp) {
        this.currentXp = currentXp;
    }
}