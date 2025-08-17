package org.nakii.mmorpg.player;

/**
 * Holds transient data for a player that is not directly derived from stats or gear.
 * This includes environmental effects like current cold and heat levels.
 */
public class PlayerState {

    private double coldLevel = 0;
    private double heatLevel = 0;

    public double getColdLevel() {
        return coldLevel;
    }

    public void setColdLevel(double coldLevel) {
        this.coldLevel = Math.max(0, coldLevel); // Ensure level doesn't go below 0
    }

    public void addCold(double amount) {
        setColdLevel(this.coldLevel + amount);
    }

    public double getHeatLevel() {
        return heatLevel;
    }

    public void setHeatLevel(double heatLevel) {
        this.heatLevel = Math.max(0, heatLevel); // Ensure level doesn't go below 0
    }

    public void addHeat(double amount) {
        setHeatLevel(this.heatLevel + amount);
    }
}