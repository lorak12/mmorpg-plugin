package org.nakii.mmorpg.enchantment;

import org.bukkit.configuration.ConfigurationSection;
import org.nakii.mmorpg.MMORPGCore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomEnchantment {

    private final String id;
    private final String displayName;
    private final int maxLevel;
    private final List<String> description;
    private final List<String> incompatibilities;
    private final String customLogicKey;
    private final List<ApplicableType> applicableTypes;

    // NEW: A map to hold data for each specific level
    private final Map<Integer, EnchantmentLevelData> levelDataMap = new HashMap<>();
    private final Map<String, String> statModifiers = new HashMap<>();

    /**
     * A simple, immutable record to hold data for a single enchantment level.
     */
    public record EnchantmentLevelData(int value, int cost, int bookshelfReq, int skillReq) {}

    public CustomEnchantment(String id, ConfigurationSection section) {
        this.id = id;
        this.displayName = section.getString("display-name", "Unnamed Enchant");
        this.maxLevel = section.getInt("max-level", 1);
        this.description = section.getStringList("description");
        this.incompatibilities = section.getStringList("incompatibilities");
        this.customLogicKey = section.getString("custom-logic-key", null);
        this.applicableTypes = section.getStringList("applicable-types").stream()
                .map(s -> {
                    try {
                        return ApplicableType.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        MMORPGCore.getInstance().getLogger().warning("Invalid applicable-type '" + s + "' for enchantment '" + id + "'.");
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        ConfigurationSection statsSection = section.getConfigurationSection("stat-modifiers");
        if (statsSection != null) {
            for (String statKey : statsSection.getKeys(false)) {
                statModifiers.put(statKey.toUpperCase(), statsSection.getString(statKey));
            }
        }


        // NEW: Parsing logic for the "levels" section
        ConfigurationSection levelsSection = section.getConfigurationSection("levels");
        if (levelsSection != null) {
            for (String levelKey : levelsSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(levelKey);
                    ConfigurationSection levelSection = levelsSection.getConfigurationSection(levelKey);
                    if (levelSection != null) {
                        int value = levelSection.getInt("value", 0);
                        int cost = levelSection.getInt("cost", 999);
                        int bookshelfReq = levelSection.getInt("bookshelf-req", 999);
                        int skillReq = levelSection.getInt("skill-req", 999);
                        levelDataMap.put(level, new EnchantmentLevelData(value, cost, bookshelfReq, skillReq));
                    }
                } catch (NumberFormatException e) {
                    MMORPGCore.getInstance().getLogger().warning("Invalid level key '" + levelKey + "' for enchantment '" + id + "'. Must be a number.");
                }
            }
        }
    }

    /**
     * Gets the description for a specific level, with the {value} placeholder replaced.
     * @param level The enchantment level.
     * @return A list of formatted lore lines.
     */
    public List<String> getDescription(int level) {
        if (description.isEmpty()) {
            return Collections.emptyList();
        }
        int value = getValue(level);
        return description.stream()
                .map(line -> line.replace("{value}", String.valueOf(value)))
                .collect(Collectors.toList());
    }

    // --- NEW GETTERS for level-specific data ---

    private EnchantmentLevelData getDataForLevel(int level) {
        return levelDataMap.getOrDefault(level, null);
    }

    public int getValue(int level) {
        EnchantmentLevelData data = getDataForLevel(level);
        return (data != null) ? data.value() : 0;
    }

    public int getCost(int level) {
        EnchantmentLevelData data = getDataForLevel(level);
        return (data != null) ? data.cost() : 999; // Return a high cost if not defined
    }

    public int getBookshelfRequirement(int level) {
        EnchantmentLevelData data = getDataForLevel(level);
        return (data != null) ? data.bookshelfReq() : 999;
    }

    public int getSkillRequirement(int level) {
        EnchantmentLevelData data = getDataForLevel(level);
        return (data != null) ? data.skillReq() : 999;
    }

    public String getCustomLogicKey() {
        return customLogicKey;
    }

    /**
     * Returns the map of stat modifier formulas for this enchantment.
     * The key is the stat name (e.g., "HEALTH") and the value is the formula string (e.g., "15 * level").
     * @return An unmodifiable map of stat modifiers.
     */
    public Map<String, String> getStatModifiers() {
        return Collections.unmodifiableMap(statModifiers);
    }
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaxLevel() { return maxLevel; }
    public List<String> getDescription() { return Collections.unmodifiableList(description); }
    public List<String> getIncompatibilities() { return Collections.unmodifiableList(incompatibilities); }
    public List<ApplicableType> getApplicableTypes() { return Collections.unmodifiableList(applicableTypes); }
}