package org.nakii.mmorpg.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.scoreboard.ScoreboardProvider;
import org.nakii.mmorpg.slayer.ActiveSlayerQuest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SlayerManager implements ScoreboardProvider {

    private final MMORPGCore plugin;
    private FileConfiguration slayerConfig;
    private final Map<UUID, ActiveSlayerQuest> activeQuests = new HashMap<>();

    public SlayerManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadSlayerConfig();
    }

    @Override // <-- Implement the interface method
    public List<String> getScoreboardLines(Player player) {
        ActiveSlayerQuest quest = getActiveQuest(player);
        if (quest == null) return List.of(); // Should not happen if this is the provider

        String bossName = getSlayerConfig().getString(quest.getSlayerType() + ".display-name");

        // We can add logic here for the "Slay the boss" state
        // For now, it shows progress.
        return List.of(
                "<white>Slayer Quest</white>",
                bossName,
                String.format("<white>(<yellow>%,.0f</yellow>/<red>%,d</red>) <gray>Combat XP</gray>",
                        quest.getCurrentXp(), quest.getXpToSpawn())
        );
    }

    private void loadSlayerConfig() {
        File file = new File(plugin.getDataFolder(), "slayers.yml");
        if (!file.exists()) {
            plugin.saveResource("slayers.yml", false);
        }
        this.slayerConfig = YamlConfiguration.loadConfiguration(file);
    }

    public boolean hasActiveQuest(Player player) {
        return activeQuests.containsKey(player.getUniqueId());
    }

    public ActiveSlayerQuest getActiveQuest(Player player) {
        return activeQuests.get(player.getUniqueId());
    }

    public void startQuest(Player player, String slayerType, int tier) {
        if (hasActiveQuest(player)) {
            // Player already has a quest
            return;
        }

        int xpToSpawn = slayerConfig.getInt(slayerType + ".tiers." + tier + ".xp-to-spawn");
        // Here you would deduct the coin cost from the player's balance.

        ActiveSlayerQuest quest = new ActiveSlayerQuest(slayerType, tier, xpToSpawn);
        activeQuests.put(player.getUniqueId(), quest);
        plugin.getScoreboardManager().setActiveProvider(player, this);
        player.sendMessage("Slayer quest started!"); // Placeholder message
    }

    public void endQuest(Player player) {
        activeQuests.remove(player.getUniqueId());
        plugin.getScoreboardManager().clearActiveProvider(player);
    }

    public String getTargetCategoryForQuest(ActiveSlayerQuest quest) {
        return slayerConfig.getString(quest.getSlayerType() + ".target-category");
    }

    public void spawnSlayerBoss(Player player, ActiveSlayerQuest quest) {
        String bossId = slayerConfig.getString(quest.getSlayerType() + ".tiers." + quest.getTier() + ".boss-id");
        if (bossId != null) {
            plugin.getMobManager().spawnMob(bossId, player.getLocation());
            player.sendMessage("Slayer boss has spawned!"); // Placeholder
            endQuest(player); // End the quest once the boss is spawned
        }
    }

    public FileConfiguration getSlayerConfig() {
        return slayerConfig;
    }
}