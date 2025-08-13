package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerState;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.utils.ChatUtils;
import java.text.DecimalFormat;

public class HUDManager {

    private final MMORPGCore plugin;
    private static final DecimalFormat df = new DecimalFormat("#");

    public HUDManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void startHUDTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isOnline()) continue;
                    updatePlayerUI(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void updatePlayerUI(Player player) {
        // --- 1. Update the Action Bar ---
        updateHud(player);

        // --- 2. Synchronize the Vanilla Health Bar ---
        syncVisualHealth(player);
    }

    private void syncVisualHealth(Player player) {
        if (player.isDead()) return;

        double currentCustomHealth = plugin.getHealthManager().getCurrentHealth(player);
        double maxCustomHealth = plugin.getHealthManager().getMaxHealth(player);

        // This is the maximum value of the vanilla health bar (e.g., 100 from stats)
        double vanillaMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        // Calculate what the vanilla health should be as a percentage of our custom health
        double targetVanillaHealth = (currentCustomHealth / maxCustomHealth) * vanillaMaxHealth;

        // Prevent the player from appearing dead when they have a tiny amount of custom health
        if (targetVanillaHealth < 1.0 && currentCustomHealth > 0) {
            targetVanillaHealth = 1.0;
        }

        // Apply the synchronized value, ensuring it doesn't exceed the vanilla max
        player.setHealth(Math.min(targetVanillaHealth, vanillaMaxHealth));
    }

    public void updateHud(Player player) {
        PlayerStats stats = plugin.getStatsManager().getStats(player);
        double currentHealth = plugin.getHealthManager().getCurrentHealth(player);
        double maxHealth = stats.getHealth();
        double defense = stats.getDefense();
        double mana = stats.getIntelligence();
        PlayerState state = plugin.getPlayerStateManager().getState(player);
        double cold = state.getCold();
        double heat = state.getHeat();

        String healthBar = "<dark_red>❤ " + df.format(currentHealth) + "/" + df.format(maxHealth) + "</dark_red>";
        String defenseBar = "<green>❈ " + df.format(defense) + "</green>";
        String manaBar = "<blue>✎ " + df.format(mana) + "/" + df.format(mana) + "</blue>";

        // Environment Bar (only appears when needed)
        String environmentBar = "";
        if (heat > 0) {
            environmentBar = " <gold>♨ " + df.format(heat) + "%</gold>";
        } else if (cold > 0) {
            environmentBar = " <aqua>❄ " + df.format(cold) + "%</aqua>";
        }

        // --- Combine Components into Final Message ---
        // Example Layout: ❤ 150/200     ❈ 75     ✎ 80/100 ♨ 45%
        String actionBarMessage = healthBar + "    " + defenseBar + "    " + manaBar + environmentBar;

        // Send the formatted message to the player's action bar
        player.sendActionBar(ChatUtils.format(actionBarMessage));
    }

    /**
     * Spawns a floating holographic damage indicator at a location.
     * @param location Where to spawn the indicator.
     * @param damage The amount of damage to display.
     * @param isCrit True if the damage was a critical hit.
     */
    public void showDamageIndicator(Location location, double damage, boolean isCrit) {
        if (location.getWorld() == null) return;

        location.add(Math.random() * 0.5 - 0.25, 0.5 + (Math.random() * 0.5), Math.random() * 0.5 - 0.25);

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setSmall(true);

        String damageText = String.format("%.0f", damage);
        String formattedText = isCrit ? "<red><b>✧" + damageText + "✧</b></red>" : "<gray>" + damageText + "</gray>";

        armorStand.customName(ChatUtils.format(formattedText));
        armorStand.setCustomNameVisible(true);

        // Schedule the armor stand to be removed after a short time
        plugin.getServer().getScheduler().runTaskLater(plugin, armorStand::remove, 30L); // 1.5 seconds
    }
}