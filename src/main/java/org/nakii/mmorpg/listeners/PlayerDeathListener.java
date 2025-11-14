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
import org.nakii.mmorpg.util.ChatUtils;

import java.text.NumberFormat;

public class PlayerDeathListener implements Listener {

    private final EconomyManager economyManager;
    private final MMORPGCore plugin;

    public PlayerDeathListener(MMORPGCore plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
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
        new BukkitRunnable() {
            @Override
            public void run() {
                //TODO: These effects are now managed by PlayerMovementTracker,
                // but we can re-apply here as a backup.
                // A better solution would be to call a method in a central player state manager.
                if (!player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 4, false, false, false));
                }
                if (!player.hasPotionEffect(PotionEffectType.HASTE)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, PotionEffect.INFINITE_DURATION, 2, false, false, false));
                }
            }
        }.runTaskLater(plugin, 1L);
    }
}