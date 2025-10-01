package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.UUID; // --- NEW ---
import java.util.concurrent.ConcurrentHashMap; // --- NEW ---

public class HUDManager {

    private final MMORPGCore plugin;

    // --- NEW: A map to store temporary, prioritized messages for players. ---
    private final ConcurrentHashMap<UUID, PriorityMessage> temporaryMessages;

    public HUDManager(MMORPGCore plugin) {
        this.plugin = plugin;
        // --- NEW: Initialize the map in the constructor. ---
        this.temporaryMessages = new ConcurrentHashMap<>();
    }

    /**
     * Starts the repeating task that updates the action bar HUD for all players.
     */
    public void startHUDTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // --- MODIFIED: The core HUD logic is now smarter. ---

                    // Check if there's a temporary message for the player.
                    PriorityMessage tempMessage = temporaryMessages.get(player.getUniqueId());

                    if (tempMessage != null) {
                        // If a message exists, check if it's expired.
                        if (tempMessage.isExpired()) {
                            // It's expired, so remove it. The default HUD will be shown below.
                            temporaryMessages.remove(player.getUniqueId());
                            updateDefaultHud(player); // Show default HUD immediately
                        } else {
                            // It's not expired, so show the temporary message and continue to the next player.
                            player.sendActionBar(tempMessage.message());
                        }
                    } else {
                        // No temporary message exists, so show the default stats HUD.
                        updateDefaultHud(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }

    /**
     * --- NEW: The public method you requested for temporary messages. ---
     * Displays a message on the action bar for a specific duration.
     * After the duration, the bar will revert to the default stats display.
     *
     * @param player          The player to send the message to.
     * @param message         The MiniMessage string to display.
     * @param durationSeconds The time in seconds to display the message.
     */
    public void updateActionBar(Player player, String message, int durationSeconds) {
        Component formattedMessage = ChatUtils.format(message);
        PriorityMessage priorityMessage = new PriorityMessage(formattedMessage, durationSeconds * 1000L);
        temporaryMessages.put(player.getUniqueId(), priorityMessage);
    }


    /**
     * --- RENAMED for clarity: (from updateHud to updateDefaultHud) ---
     * Constructs and sends the action bar component for a single player.
     * @param player The player whose HUD to update.
     */
    public void updateDefaultHud(Player player) {
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH) != null ?
            player.getAttribute(Attribute.MAX_HEALTH).getValue() : 100.0;

        PlayerStats stats = plugin.getStatsManager().getStats(player);
        double defense = stats.getDefense();
        double currentMana = 100; // Placeholder
        double maxMana = stats.getIntelligence(); // Placeholder

        Component healthComponent = Component.text(String.format("❤ %.0f/%.0f HP", currentHealth, maxHealth), NamedTextColor.RED);
        Component defenseComponent = Component.text(String.format("❈ %.0f Defense", defense), NamedTextColor.GREEN);
        Component manaComponent = Component.text(String.format("✎ %.0f/%.0f Mana", currentMana, maxMana), NamedTextColor.AQUA);

        Component actionBar = healthComponent
                .append(Component.text("    "))
                .append(defenseComponent)
                .append(Component.text("    "))
                .append(manaComponent);

        player.sendActionBar(actionBar);
    }

    /**
     * Spawns a floating holographic damage indicator at a location.
     * --- NO CHANGES MADE TO THIS METHOD ---
     */
    public void showDamageIndicator(Location location, double damage, boolean isCrit) {
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

    // --- NEW: Private inner record for holding temporary message data. ---
    // It's clean to keep it inside HUDManager since nothing else uses it.
    private record PriorityMessage(Component message, long expirationTime) {
        private PriorityMessage {
            if (message == null) {
                throw new IllegalArgumentException("Message cannot be null");
            }
            if (expirationTime < 0) {
                throw new IllegalArgumentException("Expiration time cannot be negative");
            }
        }

        public static PriorityMessage withDuration(Component message, long durationMillis) {
            return new PriorityMessage(message, System.currentTimeMillis() + durationMillis);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > this.expirationTime;
        }
    }
}

