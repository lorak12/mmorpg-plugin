package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
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
import org.nakii.mmorpg.enchantment.ApplicableType;
import org.nakii.mmorpg.util.FormattingUtils;
import org.nakii.mmorpg.util.Keys;

import java.util.stream.Collectors;

public class EnchantmentManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomEnchantment> enchantmentRegistry = new HashMap<>();
    private final Gson gson;

    public EnchantmentManager(MMORPGCore plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
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
        String json = item.getItemMeta().getPersistentDataContainer().get(Keys.CUSTOM_ENCHANTS, PersistentDataType.STRING);
        if (json == null) return new HashMap<>();
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void setEnchantments(ItemStack item, Map<String, Integer> enchantments) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        String json = gson.toJson(enchantments);
        meta.getPersistentDataContainer().set(Keys.CUSTOM_ENCHANTS, PersistentDataType.STRING, json);
        item.setItemMeta(meta);

        // Update the glint based on whether there are enchantments
        updateGlint(item);
    }

    private void updateGlint(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        Map<String, Integer> enchants = getEnchantments(item);

        if (!enchants.isEmpty()) {
            if (!meta.hasEnchant(Enchantment.LUCK_OF_THE_SEA)) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        } else {
            if (meta.hasEnchant(Enchantment.LUCK_OF_THE_SEA)) {
                meta.removeEnchant(Enchantment.LUCK_OF_THE_SEA);
            }
        }
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


    /**
     * --- THIS IS THE UPDATED METHOD ---
     * Gets a formatted string for an item's lore representing all its custom enchantments.
     * @param item The ItemStack to check.
     * @param viewer The Player viewing the item (can be null if no player context).
     * @return A MiniMessage-formatted string, or null if no enchantments.
     */
    public String getFormattedEnchantLine(ItemStack item, Player viewer) {
        Map<String, Integer> enchants = getEnchantments(item);
        if (enchants.isEmpty()) {
            return null;
        }

        StringJoiner enchantJoiner = new StringJoiner("<gray>, </gray>");
        List<String> sortedEnchantIds = new ArrayList<>(enchants.keySet());
        Collections.sort(sortedEnchantIds);

        for (String enchantId : sortedEnchantIds) {
            CustomEnchantment enchant = getEnchantment(enchantId);
            if (enchant != null) {
                String enchantText = enchant.getDisplayName() + " " + FormattingUtils.toRoman(enchants.get(enchantId));

                //TODO: --- NEW LOGIC: Check requirements ---
                // Here you would check if the player meets the skill requirement for the enchant.
                // For now, let's just show an example by making it red if they don't.
                boolean meetsReq = true; // Placeholder for SkillManager check
                if (viewer != null) {
                    // meetsReq = plugin.getSkillManager().getLevel(viewer, enchant.getRequiredSkill()) >= enchant.getRequiredSkillLevel();
                }

                if (meetsReq) {
                    enchantJoiner.add(enchantText);
                } else {
                    // If the player doesn't meet the requirement, show the enchant in red.
                    enchantJoiner.add("<red>" + enchantText + "</red>");
                }
            }
        }

        return "<blue>" + enchantJoiner + "</blue>";
    }
}