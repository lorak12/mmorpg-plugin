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
import org.nakii.mmorpg.util.ChatUtils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HUDManager {

    private final MMORPGCore plugin;
    private final StatsManager statsManager;
    private final PlayerManager playerManager;
    private final ConcurrentHashMap<UUID, PriorityMessage> temporaryMessages;

    public HUDManager(MMORPGCore plugin, StatsManager statsManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.playerManager = playerManager;
        this.temporaryMessages = new ConcurrentHashMap<>();
    }

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
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void updateActionBar(Player player, String message, int durationSeconds) {
        Component formattedMessage = ChatUtils.format(message);
        PriorityMessage priorityMessage = PriorityMessage.withDuration(formattedMessage, durationSeconds * 1000L);
        temporaryMessages.put(player.getUniqueId(), priorityMessage);
    }

    public void updateDefaultHud(Player player) {
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        PlayerStats stats = statsManager.getStats(player);
        double defense = stats.getDefense();
        double currentMana = playerManager.getCurrentMana(player);
        double maxMana = stats.getIntelligence();

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
            String gradientTag = generateCritGradientTag(damage);
            formattedText = "<white>✧</white> " + gradientTag + damageText + "</gradient> <white>✧</white>";
        } else {
            formattedText = "<gray>" + damageText + "</gray>";
        }

        armorStand.customName(ChatUtils.format(formattedText));
        armorStand.setCustomNameVisible(true);
        plugin.getServer().getScheduler().runTaskLater(plugin, armorStand::remove, 30L);
    }

    private record PriorityMessage(Component message, long expirationTime) {
        public static PriorityMessage withDuration(Component message, long durationMillis) {
            return new PriorityMessage(message, System.currentTimeMillis() + durationMillis);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > this.expirationTime;
        }
    }

    private String generateCritGradientTag(double damage) {
        final String white = "#FFFFFF";
        final String lightYellow = "#FFFF88";
        final String yellow = "#FFFF00";
        final String orange = "#FFA500";
        final String red = "#FF0000";
        String gradientColors;

        if (damage >= 100_000) {
            gradientColors = String.join(":", white, lightYellow, yellow, orange, red, orange, yellow, lightYellow, white);
        } else if (damage >= 10_000) {
            gradientColors = String.join(":", white, lightYellow, yellow, orange, yellow, lightYellow, white);
        } else {
            gradientColors = String.join(":", white, lightYellow, yellow, lightYellow, white);
        }
        return "<gradient:" + gradientColors + ">";
    }
}