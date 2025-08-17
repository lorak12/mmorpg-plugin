package org.nakii.mmorpg.mob;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.nakii.mmorpg.player.Stat;

import java.util.*;

public class CustomMobTemplate {

    // A simple record to hold loot table entry data
    public record LootDrop(String itemId, String quantity, double chance, boolean magicFind) {}

    private final String id;
    private final EntityType entityType;
    private final String displayName;
    private final int level;
    private final String mobCategory;
    private final Map<Stat, Double> stats;
    private final Map<String, String> equipment;
    private final List<LootDrop> lootTable;
    private final int slayerXp;

    public CustomMobTemplate(String id, ConfigurationSection config) {
        this.id = id;
        this.entityType = EntityType.valueOf(config.getString("type", "ZOMBIE").toUpperCase());
        this.displayName = config.getString("display-name", "Unnamed Mob");
        this.level = config.getInt("level", 1);
        this.mobCategory = config.getString("category", "UNCATEGORIZED").toUpperCase();
        this.slayerXp = config.getInt("slayer-xp", 0);

        // Parse Stats
        this.stats = new EnumMap<>(Stat.class);
        ConfigurationSection statsSection = config.getConfigurationSection("stats");
        if (statsSection != null) {
            for (String key : statsSection.getKeys(false)) {
                try {
                    stats.put(Stat.valueOf(key.toUpperCase()), statsSection.getDouble(key));
                } catch (IllegalArgumentException e) { /* Log warning */ }
            }
        }

        // Parse Equipment
        this.equipment = new HashMap<>();
        ConfigurationSection equipmentSection = config.getConfigurationSection("equipment");
        if (equipmentSection != null) {
            for (String key : equipmentSection.getKeys(false)) {
                equipment.put(key, equipmentSection.getString(key));
            }
        }

        // Parse Loot Table
        this.lootTable = new ArrayList<>();
        List<Map<?, ?>> lootMaps = config.getMapList("loot-table");
        for (Map<?, ?> map : lootMaps) {
            // --- THE DEFINITIVE FIX ---
            // Get the value from the map as an Object.
            Object magicFindObj = map.get("magic-find");
            // Check if it's null. If it is, default to false. Otherwise, cast it.
            boolean magicFind = (magicFindObj != null) ? (Boolean) magicFindObj : false;

            lootTable.add(new LootDrop(
                    (String) map.get("item"),
                    String.valueOf(map.get("quantity")),
                    ((Number) map.get("chance")).doubleValue(),
                    magicFind
            ));
        }
    }

    // --- Getters ---
    public String getId() { return id; }
    public EntityType getEntityType() { return entityType; }
    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }
    public String getMobCategory() { return mobCategory; }
    public double getStat(Stat stat) { return stats.getOrDefault(stat, 0.0); }
    public Optional<Map<String, String>> getEquipment() { return Optional.ofNullable(equipment.isEmpty() ? null : equipment); }
    public List<LootDrop> getLootTable() { return lootTable; }
    public int getSlayerXp() {
        return this.slayerXp;
    }
}