package org.nakii.mmorpg.item;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.Stat;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A template holding all the parsed data for a custom item from its YAML definition.
 * This object is immutable and serves as the "blueprint" for creating ItemStacks.
 */
public class CustomItemTemplate {

    // Core Properties
    private final String id;
    private final Material material;
    private final String displayName;
    private final Rarity rarity;
    private final List<String> staticLore;
    private final boolean unbreakable;
    private final List<String> flags;
    private final String itemType;
    private final Color leatherColor; // For leather armor
    private final int customModelData;

    // Gameplay Properties
    private final Map<Stat, Double> stats;
    private final List<String> requirements;
    private final List<String> passiveEffectLore;
    private final double sellPrice;

    // Complex Object Properties (nullable)
    private final ArmorSetInfo armorSetInfo;
    private final AbilityInfo abilityInfo;
    private final CraftingRecipeInfo craftingRecipeInfo;

    public CustomItemTemplate(String id, ConfigurationSection config) {
        this.id = id;
        this.material = Material.getMaterial(config.getString("material", "STONE").toUpperCase());
        this.displayName = config.getString("display-name", "Unnamed Item");
        this.rarity = Rarity.fromString(config.getString("rarity", "COMMON"));
        this.staticLore = config.getStringList("lore");
        this.unbreakable = config.getBoolean("unbreakable", true);
        this.flags = config.getStringList("flags").stream().map(String::toUpperCase).collect(Collectors.toList());
        this.itemType = config.getString("item-type");
        this.customModelData = config.getInt("custom-model-data", 0);

        // Parse leather armor color from hex string
        String colorString = config.getString("color");
        this.leatherColor = (colorString != null) ? Color.fromRGB(Integer.parseInt(colorString.substring(1), 16)) : null;

        // Parse Stats
        this.stats = new EnumMap<>(Stat.class);
        ConfigurationSection statsSection = config.getConfigurationSection("stats");
        if (statsSection != null) {
            for (String key : statsSection.getKeys(false)) {
                try {
                    Stat stat = Stat.valueOf(key.toUpperCase());
                    stats.put(stat, statsSection.getDouble(key));
                } catch (IllegalArgumentException e) {
                    MMORPGCore.getInstance().getLogger().warning("Invalid stat '" + key + "' for item '" + id + "'.");
                }
            }
        }

        this.requirements = config.getStringList("requirements");
        this.passiveEffectLore = config.getStringList("passive-effect");
        this.sellPrice = config.getDouble("sell-price", 0.0);

        // Parse complex nested sections
        this.armorSetInfo = config.isConfigurationSection("armor-set") ? new ArmorSetInfo(config.getConfigurationSection("armor-set")) : null;
        this.abilityInfo = config.isConfigurationSection("ability") ? new AbilityInfo(config.getConfigurationSection("ability")) : null;
        this.craftingRecipeInfo = config.isConfigurationSection("crafting-recipe") ? new CraftingRecipeInfo(config.getConfigurationSection("crafting-recipe")) : null;
    }

    // --- Sub-records for nested data ---

    public record ArmorSetInfo(String id, String name, int pieces, List<String> bonusDescription, Map<Stat, Double> fullSetStats) {
        public ArmorSetInfo(ConfigurationSection config) {
            this(
                    config.getString("id"),
                    config.getString("name"),
                    config.getInt("pieces"),
                    config.getStringList("bonus-description"),
                    parseStats(config.getConfigurationSection("full-set-stats"))
            );
        }
    }

    public record AbilityInfo(String key, String name, double manaCost, int cooldownSeconds, List<String> description) {
        public AbilityInfo(ConfigurationSection config) {
            this(
                    config.getString("key"),
                    config.getString("name"),
                    config.getDouble("mana-cost"),
                    config.getInt("cooldown-seconds"),
                    config.getStringList("description")
            );
        }
    }

    public record CraftingRecipeInfo(List<String> shape, Map<Character, String> ingredients) {
        public CraftingRecipeInfo(ConfigurationSection config) {
            this(
                    config.getStringList("shape"),
                    parseIngredients(config.getConfigurationSection("ingredients"))
            );
        }

        private static Map<Character, String> parseIngredients(ConfigurationSection config) {
            Map<Character, String> map = new HashMap<>();
            if (config != null) {
                for (String key : config.getKeys(false)) {
                    map.put(key.charAt(0), config.getString(key));
                }
            }
            return map;
        }
    }

    // Helper method for parsing stat blocks, used in multiple places
    private static Map<Stat, Double> parseStats(ConfigurationSection config) {
        Map<Stat, Double> stats = new EnumMap<>(Stat.class);
        if (config != null) {
            for (String key : config.getKeys(false)) {
                try {
                    stats.put(Stat.valueOf(key.toUpperCase()), config.getDouble(key));
                } catch (IllegalArgumentException e) { /* Log warning */ }
            }
        }
        return stats;
    }

    // --- GETTERS for all properties ---

    public String getId() { return id; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return displayName; }
    public Rarity getRarity() { return rarity; }
    public List<String> getStaticLore() { return staticLore; }
    public boolean isUnbreakable() { return unbreakable; }
    public boolean hasFlag(String flag) { return flags.contains(flag.toUpperCase()); }
    public String getItemType() { return itemType; }
    public Color getLeatherColor() { return leatherColor; }
    public Map<Stat, Double> getStats() { return Collections.unmodifiableMap(stats); }
    public List<String> getRequirements() { return Collections.unmodifiableList(requirements); }
    public List<String> getPassiveEffectLore() { return Collections.unmodifiableList(passiveEffectLore); }
    public double getSellPrice() { return sellPrice; }
    public Optional<ArmorSetInfo> getArmorSetInfo() { return Optional.ofNullable(armorSetInfo); }
    public Optional<AbilityInfo> getAbilityInfo() { return Optional.ofNullable(abilityInfo); }
    public Optional<CraftingRecipeInfo> getCraftingRecipeInfo() { return Optional.ofNullable(craftingRecipeInfo); }
    public int getCustomModelData() { return customModelData; }
}