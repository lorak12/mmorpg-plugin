package org.nakii.mmorpg.managers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.entity.CustomMob;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MobManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomMob> mobRegistry = new HashMap<>();

    public MobManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadMobs();
    }

    public void loadMobs() {
        mobRegistry.clear();
        File mobsFile = new File(plugin.getDataFolder(), "mobs.yml");
        if (!mobsFile.exists()) {
            plugin.saveResource("mobs.yml", false);
        }
        FileConfiguration mobConfig = YamlConfiguration.loadConfiguration(mobsFile);
        for (String key : mobConfig.getKeys(false)) {
            ConfigurationSection section = mobConfig.getConfigurationSection(key);
            if (section != null) {
                mobRegistry.put(key.toLowerCase(), new CustomMob(key, section));
            }
        }
        plugin.getLogger().info("Loaded " + mobRegistry.size() + " custom mob definitions.");
    }

    public LivingEntity spawnMob(String mobId, Location location) {
        CustomMob customMob = mobRegistry.get(mobId.toLowerCase());
        if (customMob == null) {
            plugin.getLogger().warning("Attempted to spawn unknown mob with ID: " + mobId);
            return null;
        }

        EntityType baseType = EntityType.valueOf(customMob.getBaseType().toUpperCase());
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, baseType);

        // Apply Disguise
        if (plugin.isLibsDisguisesEnabled()) {
            applyDisguise(entity, customMob);
        }

        // Set persistent data to identify the mob
        entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "mob_id"), PersistentDataType.STRING, customMob.getId());

        // Apply stats
        plugin.getHealthManager().registerEntity(entity, customMob.getStatsConfig().getDouble("health", 20.0));

        // Apply equipment
        applyEquipment(entity, customMob);

        // Set name and other properties
        entity.setCustomName(ChatColor.translateAlternateColorCodes('&', customMob.getDisplayName()));
        entity.setCustomNameVisible(true);
        entity.setCanPickupItems(false);
        entity.setRemoveWhenFarAway(false); // Important for bosses

        return entity;
    }

    private void applyDisguise(LivingEntity entity, CustomMob customMob) {
        String disguiseName = customMob.getDisguiseType().toUpperCase();
        DisguiseType disguiseType = DisguiseType.valueOf(disguiseName);

        if (disguiseType.isPlayer()) {
            ConfigurationSection options = customMob.getConfig().getConfigurationSection("disguise_options");
            String name = options != null ? options.getString("name", "Unknown") : "Unknown";
            PlayerDisguise disguise = new PlayerDisguise(name);

            if (options != null && options.contains("skin")) {
                disguise.setSkin(options.getString("skin"));
            }
            DisguiseAPI.disguiseToAll(entity, disguise);
        } else {
            MobDisguise disguise = new MobDisguise(disguiseType);
            DisguiseAPI.disguiseToAll(entity, disguise);
        }
    }

    private void applyEquipment(LivingEntity entity, CustomMob customMob) {
        ConfigurationSection equipment = customMob.getEquipmentConfig();
        if (equipment == null || entity.getEquipment() == null) return;

        entity.getEquipment().setItemInMainHand(getItem(equipment.getString("main_hand")));
        entity.getEquipment().setItemInOffHand(getItem(equipment.getString("off_hand")));
        entity.getEquipment().setHelmet(getItem(equipment.getString("helmet")));
        entity.getEquipment().setChestplate(getItem(equipment.getString("chestplate")));
        entity.getEquipment().setLeggings(getItem(equipment.getString("leggings")));
        entity.getEquipment().setBoots(getItem(equipment.getString("boots")));

        // Prevent items from dropping on death (we'll use our own loot table)
        entity.getEquipment().setHelmetDropChance(0f);
        entity.getEquipment().setChestplateDropChance(0f);
        entity.getEquipment().setLeggingsDropChance(0f);
        entity.getEquipment().setBootsDropChance(0f);
        entity.getEquipment().setItemInMainHandDropChance(0f);
        entity.getEquipment().setItemInOffHandDropChance(0f);
    }

    private ItemStack getItem(String identifier) {
        if (identifier == null || identifier.isEmpty()) return null;
        if (identifier.startsWith("custom:")) {
            return plugin.getItemManager().createItem(identifier.substring(7), 1);
        } else {
            return new ItemStack(Material.valueOf(identifier.toUpperCase()));
        }
    }
    /**
     * BUG FIX: Added public getter for the mob registry.
     * This is required for the tab-completer to suggest mob names.
     * @return A map of all loaded custom mobs.
     */
    public Map<String, CustomMob> getMobRegistry() {
        return mobRegistry;
    }

    public CustomMob getCustomMob(String mobId) {
        return mobRegistry.get(mobId.toLowerCase());
    }

    public String getMobId(Entity entity) {
        return entity.getPersistentDataContainer().get(new NamespacedKey(plugin, "mob_id"), PersistentDataType.STRING);
    }

    public boolean isCustomMob(Entity entity) {
        return entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "mob_id"), PersistentDataType.STRING);
    }
}