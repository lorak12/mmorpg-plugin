package org.nakii.mmorpg.player;

public class PlayerData {
    private double currentMana;
    private double currentHealth;

    public PlayerData() {
        this.currentMana = 0;
        this.currentHealth = 100;
    }

    public double getCurrentMana() { return currentMana; }
    public void setCurrentMana(double currentMana) { this.currentMana = currentMana; }

    public double getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(double currentHealth) { this.currentHealth = currentHealth; }
}