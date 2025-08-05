package org.nakii.mmorpg.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ItemManager {

    private final MMORPGCore plugin;
    private final Map<String, ConfigurationSection> customItems = new HashMap<>();

    public ItemManager(MMORPGCore plugin) {
        this.plugin = plugin;
        // The directory creation is handled in the main class now.
        loadItems();
    }

    public void loadItems() {
        customItems.clear();
        File itemsFolder = new File(plugin.getDataFolder(), "items");
        File[] files = itemsFolder.listFiles();

        if (files == null) {
            plugin.getLogger().warning("Could not list files in the /items/ directory. Is it a folder?");
            return;
        }

        for (File file : files) {
            if (file.getName().endsWith(".yml")) {
                FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(file);
                for (String key : itemConfig.getKeys(false)) {
                    if (customItems.containsKey(key.toLowerCase())) {
                        plugin.getLogger().warning("Duplicate item key '" + key + "' found in " + file.getName() + ". It will be overwritten.");
                    }
                    customItems.put(key.toLowerCase(), itemConfig.getConfigurationSection(key));
                }
            }
        }
        plugin.getLogger().info("Loaded " + customItems.size() + " custom items from " + files.length + " file(s).");
    }

    public ItemStack createItem(String key, int amount) {
        key = key.toLowerCase();
        ConfigurationSection itemConfig = customItems.get(key);
        if (itemConfig == null) {
            return null;
        }

        Material material = Material.matchMaterial(itemConfig.getString("material", "STONE"));
        if (material == null) {
            plugin.getLogger().warning("Invalid material for item: " + key);
            return null;
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        // Set basic properties
        meta.setDisplayName(formatColors(itemConfig.getString("display_name", "")));
        List<String> lore = itemConfig.getStringList("lore").stream()
                .map(this::formatColors)
                .collect(Collectors.toList());
        meta.setLore(lore);
        meta.setCustomModelData(itemConfig.getInt("custom_model_data", 0));
        meta.setUnbreakable(itemConfig.getBoolean("unbreakable", false));

        // Store stats in PersistentDataContainer
        ConfigurationSection statsSection = itemConfig.getConfigurationSection("stats");
        if (statsSection != null) {
            for (String statKey : statsSection.getKeys(false)) {
                NamespacedKey p_key = new NamespacedKey(plugin, "stat_" + statKey);
                meta.getPersistentDataContainer().set(p_key, PersistentDataType.DOUBLE, statsSection.getDouble(statKey));
            }
        }

        // Add an identifier key
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "item_id"), PersistentDataType.STRING, key);

        item.setItemMeta(meta);
        return item;
    }

    public void giveItem(Player player, String key, int amount) {
        ItemStack item = createItem(key, amount);
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(formatColors("<green>You have received " + item.getItemMeta().getDisplayName() + "</green>"));
        } else {
            player.sendMessage(formatColors("<red>The item '" + key + "' does not exist.</red>"));
        }
    }

    public Map<String, ConfigurationSection> getCustomItems() {
        return customItems;
    }

    private String formatColors(String text) {
        // A simple placeholder for a proper MiniMessage implementation
        return ChatColor.translateAlternateColorCodes('&', text.replace("<red>", "&c").replace("<gray>", "&7").replace("<dark_red>", "&4").replace("<gold>", "&6").replace("<yellow>", "&e").replace("<green>", "&a").replace("<aqua>", "&b").replace("<blue>", "&9"));
    }
}