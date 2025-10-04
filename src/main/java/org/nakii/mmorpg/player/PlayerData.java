package org.nakii.mmorpg.player;

public class PlayerData {
    private double currentMana;

    public PlayerData() {
        this.currentMana = 0;
    }

    public double getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(double currentMana) {
        this.currentMana = currentMana;
    }
}