package org.nakii.mmorpg.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

import java.io.File;
import java.util.*;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.enchantment.ApplicableType;
import java.util.stream.Collectors;

public class EnchantmentManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomEnchantment> enchantmentRegistry = new HashMap<>();
    private final Gson gson;
    private final NamespacedKey ENCHANTS_KEY;


    public EnchantmentManager(MMORPGCore plugin) {
        this.plugin = plugin;

        this.gson = new Gson();
        this.ENCHANTS_KEY = new NamespacedKey(plugin, "custom_enchants");

        loadEnchantments();
    }

    public void loadEnchantments() {
        enchantmentRegistry.clear();
        File enchantsFile = new File(plugin.getDataFolder(), "enchantments.yml");
        if (!enchantsFile.exists()) {
            plugin.saveResource("enchantments.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(enchantsFile);

        ConfigurationSection enchantsSection = config.getConfigurationSection("enchantments");
        if (enchantsSection == null) {
            plugin.getLogger().warning("No 'enchantments' section found in enchantments.yml. No custom enchantments will be loaded.");
            return;
        }

        for (String key : enchantsSection.getKeys(false)) {
            try {
                CustomEnchantment enchantment = new CustomEnchantment(key, enchantsSection.getConfigurationSection(key));
                enchantmentRegistry.put(key.toLowerCase(), enchantment);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load enchantment '" + key + "'. Please check its configuration. Error: " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + enchantmentRegistry.size() + " custom enchantments.");
    }

    /**
     * Gets a custom enchantment by its ID (the key in the YAML).
     * @param id The ID of the enchantment (case-insensitive).
     * @return The CustomEnchantment object, or null if not found.
     */
    public CustomEnchantment getEnchantment(String id) {
        return enchantmentRegistry.get(id.toLowerCase());
    }

    /**
     * Gets an unmodifiable view of all registered enchantments.
     * @return A map of all enchantments.
     */
    public Map<String, CustomEnchantment> getAllEnchantments() {
        return Collections.unmodifiableMap(enchantmentRegistry);
    }

    public void addEnchantment(ItemStack item, String enchantmentId, int level) {
        Map<String, Integer> enchants = getEnchantments(item);
        enchants.put(enchantmentId.toLowerCase(), level);
        setEnchantments(item, enchants);
    }

    public void removeEnchantment(ItemStack item, String enchantmentId) {
        Map<String, Integer> enchants = getEnchantments(item);
        enchants.remove(enchantmentId.toLowerCase());
        setEnchantments(item, enchants);
    }

    public Map<String, Integer> getEnchantments(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return new HashMap<>();
        String json = item.getItemMeta().getPersistentDataContainer().get(ENCHANTS_KEY, PersistentDataType.STRING);
        if (json == null) return new HashMap<>();
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void setEnchantments(ItemStack item, Map<String, Integer> enchantments) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        String json = gson.toJson(enchantments);
        meta.getPersistentDataContainer().set(ENCHANTS_KEY, PersistentDataType.STRING, json);
        item.setItemMeta(meta);

        // This is now called every time an item's enchants are changed.
        updateItemLoreWithEnchants(item);
    }

    /**
     * Updates an item's lore to display its custom enchantments in the correct format.
     */
    private void updateItemLoreWithEnchants(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // 1. Remove all existing custom enchant lines AND any blank lines that might be left over.
        lore.removeIf(line -> line.startsWith("§9") || line.trim().isEmpty());

        Map<String, Integer> enchants = getEnchantments(item);
        if (!enchants.isEmpty()) {
            // Build the new enchantment line, e.g., "Critical V, Sharpness V"
            StringJoiner enchantJoiner = new StringJoiner(", ");
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                CustomEnchantment enchant = getEnchantment(entry.getKey());
                if (enchant != null) {
                    enchantJoiner.add(enchant.getDisplayName() + " " + toRoman(entry.getValue()));
                }
            }
            lore.add("");
            lore.add("§9" + enchantJoiner.toString());
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Determines the applicable item type for an enchantment check.
     * @return The ApplicableType enum, or null if not a supported item.
     */
    public ApplicableType getApplicableTypeForItem(Material material) {
        String typeName = material.name();
        if (typeName.endsWith("_SWORD")) return ApplicableType.SWORD;
        if (typeName.endsWith("_HELMET") || typeName.endsWith("_CHESTPLATE") || typeName.endsWith("_LEGGINGS") || typeName.endsWith("_BOOTS")) return ApplicableType.ARMOR;
        if (typeName.endsWith("_PICKAXE") || typeName.endsWith("_AXE") || typeName.endsWith("_SHOVEL") || typeName.endsWith("_HOE")) return ApplicableType.TOOL;
        if (material == Material.BOW || material == Material.CROSSBOW) return ApplicableType.BOW;
        if (material == Material.FISHING_ROD) return ApplicableType.FISHING_ROD;
        return null;
    }

    public List<CustomEnchantment> getApplicableEnchantments(ItemStack item) {
        if (item == null || item.getType().isAir()) return Collections.emptyList();
        ApplicableType type = getApplicableTypeForItem(item.getType());
        if (type == null) return Collections.emptyList();

        return enchantmentRegistry.values().stream()
                .filter(enchant -> enchant.getApplicableTypes().contains(type))
                .collect(Collectors.toList());
    }

    private String toRoman(int number) {
        if (number < 1 || number > 10) return String.valueOf(number);
        String[] numerals = {"X", "IX", "V", "IV", "I"};
        int[] values = {10, 9, 5, 4, 1};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                result.append(numerals[i]);
            }
        }
        return result.toString();
    }
}