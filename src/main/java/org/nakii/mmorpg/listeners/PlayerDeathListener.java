package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.economy.PlayerEconomy;
import org.nakii.mmorpg.managers.EconomyManager;
import org.nakii.mmorpg.managers.PlayerManager;
import org.nakii.mmorpg.managers.StatsManager;
import org.nakii.mmorpg.util.ChatUtils;

import java.text.NumberFormat;

public class PlayerDeathListener implements Listener {

    private final EconomyManager economyManager;
    private final MMORPGCore plugin;
    private final PlayerManager playerManager;
    private final StatsManager statsManager;

    public PlayerDeathListener(MMORPGCore plugin, EconomyManager economyManager, PlayerManager playerManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.playerManager = playerManager;
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerEconomy economy = economyManager.getEconomy(player);
        double currentPurse = economy.getPurse();
        double coinsLost = Math.floor(currentPurse / 2.0);

        if (coinsLost > 0) {
            economy.removePurse(coinsLost); // Use removePurse to ensure events are fired
            String formattedCoinsLost = NumberFormat.getInstance().format(coinsLost);
            player.sendMessage(ChatUtils.format("<red>You died and lost <gold>" + formattedCoinsLost + " coins</gold>!</red>"));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Schedule a task for 1 tick later to ensure all vanilla respawn logic is complete.
        new BukkitRunnable() {
            @Override
            public void run() {
                // Restore the player to 100% of their maximum MMORPG health.
                double maxHealth = statsManager.getStats(player).getHealth();
                playerManager.setCurrentHealth(player, maxHealth);

                // The HealthManager's ticking task will automatically fix their visual hearts on its next run.
            }
        }.runTaskLater(plugin, 1L);
    }
}