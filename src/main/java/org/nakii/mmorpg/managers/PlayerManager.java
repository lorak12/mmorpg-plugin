package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerData;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final MMORPGCore plugin;
    private final StatsManager statsManager;
    private final ConcurrentHashMap<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerManager(MMORPGCore plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    public void loadPlayer(Player player) {
        playerDataMap.put(player.getUniqueId(), new PlayerData());
        double maxMana = statsManager.getStats(player).getIntelligence();
        setCurrentMana(player, maxMana);
        double maxHealth = statsManager.getStats(player).getHealth();
        setCurrentHealth(player, maxHealth);
    }

    public void unloadPlayer(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public double getCurrentMana(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        return (data != null) ? data.getCurrentMana() : 0;
    }

    public void setCurrentMana(Player player, double amount) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data != null) {
            double maxMana = statsManager.getStats(player).getIntelligence();
            data.setCurrentMana(Math.max(0, Math.min(amount, maxMana)));
        }
    }

    public boolean hasEnoughMana(Player player, double amount) {
        return getCurrentMana(player) >= amount;
    }

    public void spendMana(Player player, double amount) {
        if (hasEnoughMana(player, amount)) {
            setCurrentMana(player, getCurrentMana(player) - amount);
        }
    }

    public void regenerateMana() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = playerDataMap.get(player.getUniqueId());
            if (data == null) continue;

            double maxMana = statsManager.getStats(player).getIntelligence();
            double currentMana = data.getCurrentMana();

            if (currentMana < maxMana) {
                double regenAmount = maxMana * 0.02;
                setCurrentMana(player, currentMana + regenAmount);
            }
        }
    }

    /**
     * Gets the player's current, precise MMORPG health.
     */
    public double getCurrentHealth(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        return (data != null) ? data.getCurrentHealth() : 0;
    }

    /**
     * Sets the player's current MMORPG health, clamping it between 0 and their max health.
     */
    public void setCurrentHealth(Player player, double amount) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data != null) {
            double maxHealth = statsManager.getStats(player).getHealth();
            data.setCurrentHealth(Math.max(0, Math.min(amount, maxHealth)));
        }
    }

    /**
     * Deals a specific amount of MMORPG damage to a player.
     * @param player The player to damage.
     * @param amount The amount of damage to deal.
     */
    public void dealDamage(Player player, double amount) {
        if (amount <= 0) return;
        setCurrentHealth(player, getCurrentHealth(player) - amount);
    }
}