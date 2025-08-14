package org.nakii.mmorpg.managers;

import com.google.gson.Gson;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.item.CustomItemTemplate;
import org.nakii.mmorpg.item.Rarity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomItemTemplate> itemRegistry = new HashMap<>();
    private final Gson gson = new Gson();

    // --- NBT (Persistent Data) Keys ---
    public static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(MMORPGCore.getInstance(), "item_id");
    public static final NamespacedKey RARITY_KEY = new NamespacedKey(MMORPGCore.getInstance(), "rarity");
    public static final NamespacedKey BASE_STATS_KEY = new NamespacedKey(MMORPGCore.getInstance(), "base_stats");
    public static final NamespacedKey REQUIREMENTS_KEY = new NamespacedKey(MMORPGCore.getInstance(), "requirements");
    public static final NamespacedKey ARMOR_SET_ID_KEY = new NamespacedKey(MMORPGCore.getInstance(), "armor_set_id");
    public static final NamespacedKey ARMOR_SET_STATS_KEY = new NamespacedKey(MMORPGCore.getInstance(), "armor_set_stats");
    public static final NamespacedKey REFORGE_ID_KEY = new NamespacedKey(MMORPGCore.getInstance(), "reforge_id");
    public static final NamespacedKey REFORGE_STATS_KEY = new NamespacedKey(MMORPGCore.getInstance(), "reforge_stats");

    public ItemManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadItems();
    }

    public void loadItems() {
        itemRegistry.clear();
        File itemsFolder = new File(plugin.getDataFolder(), "items");
        if (!itemsFolder.exists() || !itemsFolder.isDirectory()) {
            plugin.getLogger().warning("Items folder not found, no custom items will be loaded.");
            return;
        }
        loadItemsFromDirectory(itemsFolder);
        plugin.getLogger().info("Loaded " + itemRegistry.size() + " custom item templates.");
    }

    private void loadItemsFromDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                loadItemsFromDirectory(file); // Recursively load from subfolders
            } else if (file.getName().endsWith(".yml")) {
                var config = YamlConfiguration.loadConfiguration(file);
                for (String key : config.getKeys(false)) {
                    if (itemRegistry.containsKey(key.toUpperCase())) {
                        plugin.getLogger().warning("Duplicate item ID found: '" + key + "'. The previous entry will be overwritten.");
                    }
                    ConfigurationSection itemSection = config.getConfigurationSection(key);
                    if (itemSection != null) {
                        itemRegistry.put(key.toUpperCase(), new CustomItemTemplate(key.toUpperCase(), itemSection));
                    }
                }
            }
        }
    }

    public CustomItemTemplate getTemplate(String itemId) {
        if (itemId == null) return null;
        return itemRegistry.get(itemId.toUpperCase());
    }

    /**
     * Creates a custom ItemStack from a template, writing all necessary data to its NBT.
     * This method does NOT generate the visual lore.
     */
    public ItemStack createItemStack(String itemId) {
        CustomItemTemplate template = getTemplate(itemId);
        if (template == null) {
            // Handle requests for vanilla items
            try {
                Material mat = Material.valueOf(itemId.toUpperCase());
                ItemStack vanillaItem = new ItemStack(mat);
                ItemMeta meta = vanillaItem.getItemMeta();
                if (meta == null) return vanillaItem;

                PersistentDataContainer data = meta.getPersistentDataContainer();
                data.set(ITEM_ID_KEY, PersistentDataType.STRING, mat.name());
                data.set(RARITY_KEY, PersistentDataType.STRING, Rarity.COMMON.name()); // Default rarity
                meta.setUnbreakable(true); // Default to unbreakable

                vanillaItem.setItemMeta(meta);
                return vanillaItem;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not create item: Unknown item ID or material '" + itemId + "'.");
                return null;
            }
        }

        // --- Create item from CustomItemTemplate ---
        ItemStack item = new ItemStack(template.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        // Core Identifiers
        data.set(ITEM_ID_KEY, PersistentDataType.STRING, template.getId());
        data.set(RARITY_KEY, PersistentDataType.STRING, template.getRarity().name());
        meta.setUnbreakable(template.isUnbreakable());

        // --- Apply Custom Model Data ---
        if (template.getCustomModelData() > 0) {
            meta.setCustomModelData(template.getCustomModelData());
        }


        // Leather Armor Color
        if (meta instanceof LeatherArmorMeta leatherMeta && template.getLeatherColor() != null) {
            leatherMeta.setColor(template.getLeatherColor());
        }

        // Stats (serialize the map to a JSON string for easy storage)
        if (!template.getStats().isEmpty()) {
            Map<String, Double> stringStats = template.getStats().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
            data.set(BASE_STATS_KEY, PersistentDataType.STRING, gson.toJson(stringStats));
        }

        // Requirements
        List<String> requirements = template.getRequirements();
        if (!requirements.isEmpty()) {
            data.set(REQUIREMENTS_KEY, PersistentDataType.STRING, gson.toJson(requirements));
        }

        // Armor Set Info
        template.getArmorSetInfo().ifPresent(armorSetInfo -> {
            data.set(ARMOR_SET_ID_KEY, PersistentDataType.STRING, armorSetInfo.id());

            Map<String, Double> bonusStats = armorSetInfo.fullSetStats().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
            data.set(ARMOR_SET_STATS_KEY, PersistentDataType.STRING, gson.toJson(bonusStats));
        });

        // (We don't save Ability info to NBT here as it's static and can be read from the template)

        item.setItemMeta(meta);
        return item;
    }

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ITEM_ID_KEY, PersistentDataType.STRING);
    }

    public Map<String, CustomItemTemplate> getCustomItems(){
        return itemRegistry;
    }
}