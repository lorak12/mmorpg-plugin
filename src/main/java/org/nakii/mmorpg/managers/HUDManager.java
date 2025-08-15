package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class HUDManager {

    private final MMORPGCore plugin;

    public HUDManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the repeating task that updates the action bar HUD for all players.
     */
    public void startHUDTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateHud(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }

    /**
     * Constructs and sends the action bar component for a single player.
     * @param player The player whose HUD to update.
     */
    public void updateHud(Player player) {
        // --- THIS IS THE FIX ---
        // Get health directly from the Player object's attributes.
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        // Get custom stats from our StatsManager.
        PlayerStats stats = plugin.getStatsManager().getStats(player);
        double defense = stats.getDefense();
        // Mana would be calculated here based on Intelligence.
        double currentMana = 100; // Placeholder
        double maxMana = stats.getIntelligence(); // Placeholder

        // Build the action bar component
        Component healthComponent = Component.text(String.format("❤ %.0f/%.0f HP", currentHealth, maxHealth), NamedTextColor.RED);
        Component defenseComponent = Component.text(String.format("❈ %.0f Defense", defense), NamedTextColor.GREEN);
        Component manaComponent = Component.text(String.format("✎ %.0f/%.0f Mana", currentMana, maxMana), NamedTextColor.BLUE);

        Component actionBar = healthComponent
                .append(Component.text("    "))
                .append(defenseComponent)
                .append(Component.text("    "))
                .append(manaComponent);

        player.sendActionBar(actionBar);
    }

    /**
     * Spawns a floating holographic damage indicator at a location.
     */
    public void showDamageIndicator(Location location, double damage, boolean isCrit) {
        // This method is correct and needs no changes.
        if (location.getWorld() == null) return;
        location.add((Math.random() - 0.5) * 0.75, 0.5 + (Math.random() * 0.5), (Math.random() - 0.5) * 0.75);

        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, (as) -> {
            as.setInvisible(true);
            as.setGravity(false);
            as.setMarker(true);
            as.setSmall(true);
        });

        String damageText = String.format("%,.0f", damage);
        String formattedText = isCrit
                ? "<white>✧</white> <gradient:#FFFFFF:#FFFF00:#FF8000:#FF0000:#FF8000:#FFFF00:#FFFFFF>" + damageText + "</gradient> <white>✧</white>"
                : "<gray>" + damageText + "</gray>";

        armorStand.customName(ChatUtils.format(formattedText));
        armorStand.setCustomNameVisible(true);
        plugin.getServer().getScheduler().runTaskLater(plugin, armorStand::remove, 30L);
    }
}