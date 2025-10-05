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
import org.nakii.mmorpg.util.ChatUtils;

import java.io.File;
import java.util.ArrayList;
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
    public static final NamespacedKey PRISTINE_KEY = new NamespacedKey(MMORPGCore.getInstance(), "pristine_item");
    public static final NamespacedKey ABILITY_KEY = new NamespacedKey(MMORPGCore.getInstance(), "ability_key");
    public static final NamespacedKey PASSIVE_EFFECT_KEY = new NamespacedKey(MMORPGCore.getInstance(), "passive_effect_key");


    private final Map<Material, Rarity> defaultRarities = new HashMap<>();

    public ItemManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadItems();
        loadDefaults();
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
                return createDefaultItemStack(Material.valueOf(itemId.toUpperCase()));
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

        // --- NEW: Save Ability Key to NBT for quick lookup ---
        template.getAbilityInfo().ifPresent(abilityInfo -> {
            data.set(ABILITY_KEY, PersistentDataType.STRING, abilityInfo.key());
        });

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

    private void loadDefaults() {
        File defaultsFile = new File(plugin.getDataFolder(), "defaults.yml");
        if (!defaultsFile.exists()) {
            plugin.saveResource("defaults.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(defaultsFile);
        for (String materialName : config.getKeys(false)) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                Rarity rarity = Rarity.valueOf(config.getString(materialName, "COMMON").toUpperCase());
                defaultRarities.put(material, rarity);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material or rarity in defaults.yml: " + materialName);
            }
        }
        plugin.getLogger().info("Loaded " + defaultRarities.size() + " default item rarities.");
    }

    /**
     * Creates a formatted ItemStack for a vanilla material, applying default rarity and lore.
     * @param material The vanilla material to create an item for.
     * @return A formatted ItemStack, or null if the material is invalid.
     */
    public ItemStack createDefaultItemStack(Material material) {
        if (material == null || material.isAir()) {
            return null;
        }

        // Get the rarity from our new map, defaulting to COMMON if not specified.
        Rarity rarity = defaultRarities.getOrDefault(material, Rarity.COMMON);

        // Format the name nicely (e.g., ROTTEN_FLESH -> Rotten Flesh)
        String name = formatMaterialName(material);

        // This uses your existing ItemBuilder or manual meta creation.
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            // Apply the rarity color to the name
            meta.displayName(ChatUtils.format(rarity.getColorTag() + name));

            // Apply the standard collection lore
            List<String> lore = new ArrayList<>();
            lore.add("<dark_gray>Collection item</dark_gray>");
            lore.add(" ");
            lore.add(rarity.getDisplayTag()); // e.g., "<white><b>COMMON</b></white>"

            meta.lore(ChatUtils.formatList(lore));
        });

        return item;
    }

    private String formatMaterialName(Material material) {
        String name = material.name().replace('_', ' ');
        // Basic title case formatting
        String[] words = name.toLowerCase().split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }
        return String.join(" ", words);
    }


}