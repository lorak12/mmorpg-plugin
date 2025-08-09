package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component; // <-- Import Component
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
import org.nakii.mmorpg.utils.ChatUtils; // <-- Import ChatUtils

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


        // Set basic properties using modern Component-based methods
        meta.displayName(ChatUtils.format(itemConfig.getString("display_name", "")));

        List<Component> lore = itemConfig.getStringList("lore").stream()
                .map(ChatUtils::format) // Use the utility method for each line
                .collect(Collectors.toList());
        meta.lore(lore);

        // Use modern non-deprecated methods
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


            // Build a component-based message for the player
            Component itemName = item.displayName(); // Gets the name as a Component
            Component message = ChatUtils.format("<green>You have received ").append(itemName);
            player.sendMessage(message);


        } else {

            // Use ChatUtils for the error message
            player.sendMessage(ChatUtils.format("<red>The item '" + key + "' does not exist.</red>"));

        }
    }

    public Map<String, ConfigurationSection> getCustomItems() {
        return customItems;
    }


    // The formatColors method has been removed as it is now handled by ChatUtils.format()

}