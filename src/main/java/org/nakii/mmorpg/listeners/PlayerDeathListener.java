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
import org.nakii.mmorpg.utils.ChatUtils;

import java.text.NumberFormat;

public class PlayerDeathListener implements Listener {

    private final EconomyManager economyManager;

    public PlayerDeathListener(MMORPGCore plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerEconomy economy = economyManager.getEconomy(player);

        double currentPurse = economy.getPurse();
        double coinsLost = Math.floor(currentPurse / 2.0);

        if (coinsLost > 0) {
            economy.setPurse(currentPurse - coinsLost);
            // Format the number with commas for readability
            String formattedCoinsLost = NumberFormat.getInstance().format(coinsLost);
            player.sendMessage(ChatUtils.format("<red>You died and lost <gold>" + formattedCoinsLost + " coins</gold>!</red>"));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // ... (your existing state reset) ...

        // Re-apply mining effects
        new BukkitRunnable() {
            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 1, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 1, false, false, false));
            }
        }.runTaskLater(MMORPGCore.getInstance(), 1L); // Apply on the next tick to be safe
    }
}