package org.nakii.mmorpg.player;

public class PlayerState {
    private double cold = 0.0;
    private double heat = 0.0;

    public double getCold() { return cold; }
    public double getHeat() { return heat; }

    // --- NEW METHODS ---

    /**
     * Increases the player's heat, capped at a maximum value.
     * @param amount The amount of heat to add.
     * @param maxHeat The maximum heat allowed in the current zone.
     */
    public void increaseHeat(double amount, double maxHeat) {
        this.heat = Math.min(maxHeat, this.heat + amount);
    }

    /**
     * Increases the player's cold, capped at a maximum value.
     * @param amount The amount of cold to add.
     * @param maxCold The maximum cold allowed in the current zone.
     */
    public void increaseCold(double amount, double maxCold) {
        this.cold = Math.min(maxCold, this.cold + amount);
    }

    /**
     * Reduces the player's heat, floored at 0.
     * @param amount The amount of heat to reduce.
     */
    public void reduceHeat(double amount) {
        this.heat = Math.max(0, this.heat - amount);
    }

    /**
     * Reduces the player's cold, floored at 0.
     * @param amount The amount of cold to reduce.
     */
    public void reduceCold(double amount) {
        this.cold = Math.max(0, this.cold - amount);
    }
}