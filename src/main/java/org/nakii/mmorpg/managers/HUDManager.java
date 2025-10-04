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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HUDManager {

    private final MMORPGCore plugin;

    private final ConcurrentHashMap<UUID, PriorityMessage> temporaryMessages;

    public HUDManager(MMORPGCore plugin) {
        this.plugin = plugin;
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
                    PriorityMessage tempMessage = temporaryMessages.get(player.getUniqueId());

                    if (tempMessage != null) {
                        if (tempMessage.isExpired()) {
                            temporaryMessages.remove(player.getUniqueId());
                            updateDefaultHud(player);
                        } else {
                            player.sendActionBar(tempMessage.message());
                        }
                    } else {
                        updateDefaultHud(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }

    /**
     * Displays a message on the action bar for a specific duration.
     * After the duration, the bar will revert to the default stats display.
     *
     * @param player          The player to send the message to.
     * @param message         The MiniMessage string to display.
     * @param durationSeconds The time in seconds to display the message.
     */
    public void updateActionBar(Player player, String message, int durationSeconds) {
        Component formattedMessage = ChatUtils.format(message);
        // This should use the static factory method for clarity
        PriorityMessage priorityMessage = PriorityMessage.withDuration(formattedMessage, durationSeconds * 1000L);
        temporaryMessages.put(player.getUniqueId(), priorityMessage);
    }


    /**
     * Constructs and sends the default stats action bar for a single player.
     * @param player The player whose HUD to update.
     */
    public void updateDefaultHud(Player player) {
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH) != null ?
                player.getAttribute(Attribute.MAX_HEALTH).getValue() : 100.0;

        PlayerStats stats = plugin.getStatsManager().getStats(player);
        double defense = stats.getDefense();

        // --- MODIFIED: Get the real mana values ---
        double currentMana = plugin.getPlayerManager().getCurrentMana(player);
        double maxMana = stats.getIntelligence();

        // Use your existing formatting
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
        String formattedText;

        if (isCrit) {
            // Generate the dynamic gradient tag (e.g., "<gradient:#FFFFFF:...:#FF0000:...>")
            String gradientTag = generateCritGradientTag(damage);
            // Construct the final string
            formattedText = "<white>✧</white> " + gradientTag + damageText + "</gradient> <white>✧</white>";
        } else {
            // Non-crit damage is unchanged
            formattedText = "<gray>" + damageText + "</gray>";
        }

        armorStand.customName(ChatUtils.format(formattedText));
        armorStand.setCustomNameVisible(true);
        plugin.getServer().getScheduler().runTaskLater(plugin, armorStand::remove, 30L);
    }

    private record PriorityMessage(Component message, long expirationTime) {
        // This constructor automatically calculates the expiration time
        public static PriorityMessage withDuration(Component message, long durationMillis) {
            return new PriorityMessage(message, System.currentTimeMillis() + durationMillis);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > this.expirationTime;
        }
    }

    /**
     * --- NEW HELPER METHOD ---
     * Generates a MiniMessage gradient opening tag based on the damage amount.
     * Higher damage results in a "hotter" peak color in the gradient.
     * @param damage The amount of damage dealt.
     * @return A complete MiniMessage <gradient:...> opening tag.
     */
    private String generateCritGradientTag(double damage) {
        // Define our color palette for a smooth fade
        final String white = "#FFFFFF";
        final String lightYellow = "#FFFF88"; // An intermediate color between white and yellow
        final String yellow = "#FFFF00";
        final String orange = "#FFA500";
        final String red = "#FF0000";

        String gradientColors;

        // TIER 3: Highest damage (6 digits or more) gets a full gradient peaking at red.
        if (damage >= 100_000) {
            gradientColors = String.join(":", white, lightYellow, yellow, orange, red, orange, yellow, lightYellow, white);
        }
        // TIER 2: Medium damage gets a gradient peaking at orange.
        else if (damage >= 10_000) {
            gradientColors = String.join(":", white, lightYellow, yellow, orange, yellow, lightYellow, white);
        }
        // TIER 1: Lower damage gets a gradient peaking at yellow.
        else {
            gradientColors = String.join(":", white, lightYellow, yellow, lightYellow, white);
        }

        return "<gradient:" + gradientColors + ">";
    }
}