package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerData;
import org.nakii.mmorpg.player.Stat;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final MMORPGCore plugin;
    private final ConcurrentHashMap<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(Player player) {
        playerDataMap.put(player.getUniqueId(), new PlayerData());
        // Initialize mana to max mana on load
        double maxMana = plugin.getStatsManager().getStats(player).getIntelligence();
        setCurrentMana(player, maxMana);
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
            double maxMana = plugin.getStatsManager().getStats(player).getIntelligence();
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

    // This will be called by a BukkitRunnable in MMORPGCore
    public void regenerateMana() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = playerDataMap.get(player.getUniqueId());
            if (data == null) continue;

            double maxMana = plugin.getStatsManager().getStats(player).getIntelligence();
            double currentMana = data.getCurrentMana();

            if (currentMana < maxMana) {
                // Regenerate 2% of max mana per second (or a configured amount)
                double regenAmount = maxMana * 0.02;
                setCurrentMana(player, currentMana + regenAmount);
            }
        }
    }
}