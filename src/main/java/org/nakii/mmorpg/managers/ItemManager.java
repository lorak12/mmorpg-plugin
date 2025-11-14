package org.nakii.mmorpg.managers;

import com.google.gson.Gson;
import org.bukkit.Material;
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
import org.nakii.mmorpg.util.Keys;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomItemTemplate> itemRegistry = new HashMap<>();
    private final Map<Material, Rarity> defaultRarities = new HashMap<>();
    private final Gson gson = new Gson();

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
                loadItemsFromDirectory(file);
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

    public ItemStack createItemStack(String itemId) {
        CustomItemTemplate template = getTemplate(itemId);
        if (template == null) {
            try {
                return createVanillaItemStack(Material.valueOf(itemId.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not create item: Unknown item ID or material '" + itemId + "'.");
                return null;
            }
        }

        ItemStack item = new ItemStack(template.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(Keys.ITEM_ID, PersistentDataType.STRING, template.getId());
        data.set(Keys.RARITY, PersistentDataType.STRING, template.getRarity().name());
        meta.setUnbreakable(template.isUnbreakable());

        if (template.getCustomModelData() > 0) {
            meta.setCustomModelData(template.getCustomModelData());
        }

        if (meta instanceof LeatherArmorMeta leatherMeta && template.getLeatherColor() != null) {
            leatherMeta.setColor(template.getLeatherColor());
        }

        if (!template.getStats().isEmpty()) {
            Map<String, Double> stringStats = template.getStats().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
            data.set(Keys.BASE_STATS, PersistentDataType.STRING, gson.toJson(stringStats));
        }

        List<String> requirements = template.getRequirements();
        if (!requirements.isEmpty()) {
            data.set(Keys.REQUIREMENTS, PersistentDataType.STRING, gson.toJson(requirements));
        }

        template.getArmorSetInfo().ifPresent(armorSetInfo -> {
            data.set(Keys.ARMOR_SET_ID, PersistentDataType.STRING, armorSetInfo.id());
            Map<String, Double> bonusStats = armorSetInfo.fullSetStats().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
            data.set(Keys.ARMOR_SET_STATS, PersistentDataType.STRING, gson.toJson(bonusStats));
        });

        template.getAbilityInfo().ifPresent(abilityInfo -> {
            data.set(Keys.ABILITY_KEY, PersistentDataType.STRING, abilityInfo.key());
        });

        item.setItemMeta(meta);
        return item;
    }

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(Keys.ITEM_ID, PersistentDataType.STRING);
    }

    public Map<String, CustomItemTemplate> getCustomItems() {
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

    public ItemStack createVanillaItemStack(Material material) {
        if (material == null || material.isAir()) {
            return null;
        }
        Rarity rarity = defaultRarities.getOrDefault(material, Rarity.COMMON);
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(Keys.RARITY, PersistentDataType.STRING, rarity.name());
            meta.customName(ChatUtils.format(formatMaterialName(material)));
        });
        return item;
    }

    public String formatMaterialName(Material material) {
        String name = material.name().replace('_', ' ');
        String[] words = name.toLowerCase().split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }
        return String.join(" ", words);
    }
}