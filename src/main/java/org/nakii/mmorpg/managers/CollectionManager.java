package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.collection.PlayerCollectionData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.util.ChatUtils;
import org.nakii.mmorpg.util.FormattingUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionManager {

    private final MMORPGCore plugin;
    private final RewardManager rewardManager;
    private final SkillManager skillManager;
    private final Map<UUID, PlayerCollectionData> playerDataCache = new ConcurrentHashMap<>();
    private final Map<String, YamlConfiguration> collectionConfigs = new HashMap<>();
    private final Map<Material, String> materialToCollectionIdMap = new HashMap<>();

    public CollectionManager(MMORPGCore plugin, RewardManager rewardManager, SkillManager skillManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.skillManager = skillManager;
        loadCollections();
    }

    private void loadCollections() {
        File collectionsDir = new File(plugin.getDataFolder(), "collections");
        if (!collectionsDir.exists()) collectionsDir.mkdirs();

        // Recursively load all .yml files from subdirectories
        for (File categoryDir : collectionsDir.listFiles(File::isDirectory)) {
            for (File collectionFile : categoryDir.listFiles((dir, name) -> name.endsWith(".yml"))) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(collectionFile);
                String id = config.getString("id", collectionFile.getName().replace(".yml", "")).toUpperCase();
                collectionConfigs.put(id, config);
                // Build the material-to-ID map for fast lookups
                try {
                    Material material = Material.valueOf(config.getString("material", "").toUpperCase());
                    materialToCollectionIdMap.put(material, id);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in collection file: " + collectionFile.getName());
                }
            }
        }
        plugin.getLogger().info("Loaded " + collectionConfigs.size() + " collection configurations.");
    }

    public void addProgress(Player player, String collectionId, int amount) {
        collectionId = collectionId.toUpperCase();
        PlayerCollectionData data = getData(player);
        if (data == null) return;

        int oldAmount = data.getProgress(collectionId);
        int newAmount = oldAmount + amount;
        data.setProgress(collectionId, newAmount);

        // Check if any new tiers have been unlocked
        YamlConfiguration config = collectionConfigs.get(collectionId);
        if (config == null) return;

        ConfigurationSection tiers = config.getConfigurationSection("tiers");
        if (tiers == null) return;

        for (String tierKey : tiers.getKeys(false)) {
            int requiredAmount = tiers.getInt(tierKey + ".required");
            if (oldAmount < requiredAmount && newAmount >= requiredAmount) {
                // New tier unlocked!
                unlockTier(player, config, tierKey);
            }
        }
    }

    private void unlockTier(Player player, YamlConfiguration config, String tierKey) {
        String collectionName = config.getString("display-name", "Collection");
        List<String> rewardStrings = config.getStringList("tiers." + tierKey + ".rewards");
        int skillXp = config.getInt("tiers." + tierKey + ".skill-xp", 0);

        // --- 1. Grant Rewards using the RewardManager ---
        List<Component> grantedRewards = rewardManager.grantRewards(player, rewardStrings);

        // --- 2. Grant the tier-specific skill XP ---
        if (skillXp > 0) {
            // Skill XP for unlocking the tier is separate from SKILL_XP rewards
            try {
                String category = config.getString("category", "FARMING");
                skillManager.addXp(player, Skill.valueOf(category.toUpperCase()), skillXp);
            } catch (IllegalArgumentException ignored) {}
        }

        // --- 3. Announce the Unlock to the Player ---
        // Title announcement
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(750));
        Title title = Title.title(
                plugin.getMiniMessage().deserialize(collectionName + " Collection"),
                plugin.getMiniMessage().deserialize("<yellow>Level " + FormattingUtils.toRoman(Integer.parseInt(tierKey)) + " Unlocked!")
        );
        player.showTitle(title);

        // Detailed chat message
        player.sendMessage(ChatUtils.format("<dark_gray>-----------------------------------"));
        player.sendMessage(ChatUtils.format("  <white>COLLECTION LEVEL UP!"));
        player.sendMessage(ChatUtils.format("  " + collectionName + " <white>has reached level <yellow>" + FormattingUtils.toRoman(Integer.parseInt(tierKey)) + "</yellow>!"));
        player.sendMessage(ChatUtils.format(" "));
        player.sendMessage(ChatUtils.format("  <white>Rewards:"));

        // Display the formatted rewards from the RewardManager
        for (Component rewardLine : grantedRewards) {
            player.sendMessage(Component.text("    ").append(rewardLine));
        }

        player.sendMessage(ChatUtils.format(" "));
        player.sendMessage(ChatUtils.format("<dark_gray>-----------------------------------"));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }

    // --- Player Data Management ---
    public void addPlayer(Player player, PlayerCollectionData data) {
        playerDataCache.put(player.getUniqueId(), data);
    }

    public void removePlayer(Player player) {
        playerDataCache.remove(player.getUniqueId());
    }

    public PlayerCollectionData getData(Player player) {
        return playerDataCache.get(player.getUniqueId());
    }

    /**
     * Finds the Collection ID associated with a given vanilla material.
     * @param material The material to look up.
     * @return The collection ID (e.g., "CACTUS"), or null if no collection tracks this material.
     */
    @Nullable
    public String getCollectionId(Material material) {
        return materialToCollectionIdMap.get(material);
    }

    /**
     * Gets a list of all loaded collection configurations that belong to a specific category.
     * @param category The category to filter by (e.g., "FARMING").
     * @return A list of map entries, where the key is the collection ID and the value is the config.
     */
    public List<Map.Entry<String, YamlConfiguration>> getCollectionsByCategory(String category) {
        return collectionConfigs.entrySet().stream()
                .filter(entry -> category.equalsIgnoreCase(entry.getValue().getString("category")))
                .toList();
    }

    /**
     * Gets the specific configuration for a single collection by its ID.
     * @param collectionId The ID of the collection (e.g., "CACTUS").
     * @return The YamlConfiguration for that collection, or null if not found.
     */
    public YamlConfiguration getCollectionConfig(String collectionId) {
        return collectionConfigs.get(collectionId.toUpperCase());
    }

    /**
     * Calculates the current collection tier a player has unlocked for a specific collection.
     * @param player The player to check.
     * @param collectionId The ID of the collection (e.g., "CACTUS").
     * @return The highest tier number the player has unlocked, or 0 if they haven't started.
     */
    public int getTierForPlayer(Player player, String collectionId) {
        PlayerCollectionData data = getData(player);
        if (data == null) return 0;

        int playerAmount = data.getProgress(collectionId.toUpperCase());

        YamlConfiguration config = getCollectionConfig(collectionId.toUpperCase());
        if (config == null) return 0;

        ConfigurationSection tiers = config.getConfigurationSection("tiers");
        if (tiers == null) return 0;

        int unlockedTier = 0;
        // Iterate through the tier keys (1, 2, 3...) to find the highest unlocked tier.
        for (String tierKey : tiers.getKeys(false)) {
            int tierNum;
            try {
                tierNum = Integer.parseInt(tierKey);
            } catch (NumberFormatException e) {
                continue; // Skip non-numeric tier keys
            }

            int requiredAmount = tiers.getInt(tierKey + ".required");
            if (playerAmount >= requiredAmount) {
                // The player meets the requirement for this tier.
                // We update our variable and continue checking, as they might have a higher tier.
                if (tierNum > unlockedTier) {
                    unlockedTier = tierNum;
                }
            } else {
                // If they don't meet the requirement for this tier, they can't have any higher tiers.
                // We could break here for a small optimization, but continuing is safer.
            }
        }

        return unlockedTier;
    }


}