package org.nakii.mmorpg.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.events.PlayerBalanceChangeEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerEconomy {

    private final UUID playerUUID;

    private double purse;
    private double bank;
    private String accountTier;
    private boolean hasUnlockedUpgrades;
    private final LinkedList<Transaction> transactionHistory;

    /**
     * Constructor for creating a fresh economy object for a new player.
     * A UUID is required.
     */
    public PlayerEconomy(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.purse = 0.0;
        this.bank = 0.0;
        this.accountTier = "STARTER";
        this.hasUnlockedUpgrades = false;
        this.transactionHistory = new LinkedList<>();
    }

    /**
     * --- CONSTRUCTOR ARGUMENTS ARE NOW IN THE CORRECT ORDER ---
     * Constructor for loading a complete object from the database.
     */
    public PlayerEconomy(UUID playerUUID, double purse, double bank, String tier, boolean unlocked, List<Transaction> history) {
        this.playerUUID = playerUUID;
        this.purse = purse;
        this.bank = bank;
        this.accountTier = tier;
        this.hasUnlockedUpgrades = unlocked;
        // Ensure history is never null, even if the DB has a null value
        this.transactionHistory = (history != null) ? new LinkedList<>(history) : new LinkedList<>();
    }

    private void fireUpdateEvent() {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            Bukkit.getPluginManager().callEvent(new PlayerBalanceChangeEvent(player));
        }
    }

    // --- Getters ---
    public double getPurse() { return purse; }
    public double getBank() { return bank; }
    public String getAccountTier() { return accountTier; }
    public boolean hasUnlockedUpgrades() { return hasUnlockedUpgrades; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }

    // --- Setters ---
    public void setPurse(double amount) {
        this.purse = amount;
        fireUpdateEvent();
    }
    public void setBank(double amount) {
        this.bank = amount;
        fireUpdateEvent();
    }
    public void setAccountTier(String accountTier) { this.accountTier = accountTier; }
    public void setHasUnlockedUpgrades(boolean hasUnlockedUpgrades) { this.hasUnlockedUpgrades = hasUnlockedUpgrades; }

    // --- Transactions ---
    public void addTransaction(Transaction.TransactionType type, double amount) {
        transactionHistory.addFirst(new Transaction(type, amount, System.currentTimeMillis()));
        while (transactionHistory.size() > 10) {
            transactionHistory.removeLast();
        }
    }

    public void addPurse(double amount) {
        if (amount > 0) {
            this.purse += amount;
            fireUpdateEvent();
        }
    }

    public boolean removePurse(double amount) {
        if (amount > 0 && this.purse >= amount) {
            this.purse -= amount;
            fireUpdateEvent();
            return true;
        }
        return false;
    }

    public boolean deposit(double amount) {
        if (removePurse(amount)) { // removePurse already fires an event
            this.bank += amount;
            addTransaction(Transaction.TransactionType.DEPOSIT, amount);
            fireUpdateEvent(); // Fire a second event for the bank change
            return true;
        }
        return false;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.bank >= amount) {
            this.bank -= amount;
            this.purse += amount;
            addTransaction(Transaction.TransactionType.WITHDRAWAL, amount);
            fireUpdateEvent();
            return true;
        }
        return false;
    }
}