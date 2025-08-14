package org.nakii.mmorpg.managers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
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
import org.nakii.mmorpg.utils.ChatUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomMob> mobRegistry = new HashMap<>();
    private final Map<UUID, Integer> mobLevels = new HashMap<>();

    public MobManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadMobs();
    }

    public void registerCustomMob(LivingEntity entity, int level) {
        mobLevels.put(entity.getUniqueId(), level);
        updateHealthDisplay(entity);
    }

    public int getMobLevel(LivingEntity entity) {
        return mobLevels.getOrDefault(entity.getUniqueId(), 1);
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
        CustomMob customMob = getCustomMob(mobId);
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

        // Set persistent data
        entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "mob_id"), PersistentDataType.STRING, customMob.getId());

        // --- THIS IS THE FIX ---
        // Use the new method to set the mob's custom health from its config.
        double health = customMob.getStatsConfig().getDouble("health", 20.0);
        plugin.getHealthManager().setEntityHealth(entity, health);
        // --- END OF FIX ---

        // Apply equipment
        applyEquipment(entity, customMob);

        // Set name and other properties
        entity.setCanPickupItems(false);
        entity.setRemoveWhenFarAway(false);

        // Use the centralized name updater
        this.updateHealthDisplay(entity);

        return entity;
    }

    private void applyDisguise(LivingEntity entity, CustomMob customMob) {
        String disguiseName = customMob.getDisguiseType().toUpperCase();
        DisguiseType disguiseType;
        try {
            disguiseType = DisguiseType.valueOf(disguiseName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid disguise type '" + disguiseName + "' for mob '" + customMob.getId() + "'. Skipping disguise.");
            return;
        }

        // FINAL FIX: Check if the disguise is a player and handle it gracefully.
        if (disguiseType.isPlayer()) {
            plugin.getLogger().warning("Disguise type 'PLAYER' is not supported. Mob '" + customMob.getId() + "' will not be disguised.");
            return; // Exit the method, applying no disguise.
        }

        // This part now only runs for non-player disguises.
        if (disguiseType.isMob()) {
            MobDisguise disguise = new MobDisguise(disguiseType);
            DisguiseAPI.disguiseToAll(entity, disguise);
        } else {
            plugin.getLogger().warning("Disguise type '" + disguiseName + "' is not a mob disguise. Skipping.");
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
            return plugin.getItemManager().createItemStack(identifier.substring(7));
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

    public double getMobStrength(LivingEntity mob) {
        CustomMob customMob = getCustomMob(getMobId(mob));
        if (customMob != null && customMob.getStatsConfig() != null) {
            return customMob.getStatsConfig().getDouble("strength", 5.0);
        }
        return 5.0; // Default damage for non-custom mobs
    }
    public boolean isCustomMob(Entity entity) {
        return entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "mob_id"), PersistentDataType.STRING);
    }

    ///  The new refactior part begins here ----------------------------------------------------------------------------------

    /**
     * Updates the custom name of a mob to reflect its current health.
     * @param entity The mob whose display should be updated.
     */
    public void updateHealthDisplay(LivingEntity entity) {
        if (entity == null || entity.isDead()) {
            mobLevels.remove(entity.getUniqueId());
            return;
        }

        int level = getMobLevel(entity);
        String mobName = entity.getType().name().substring(0, 1) + entity.getType().name().substring(1).toLowerCase();
        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();

        String formattedName = String.format("<gray>[</gray><white>Lv%d</white><gray>]</gray> <red>%s</red> <green>%.0f</green><white>/</white><green>%.0f</green><red>❤</red>",
                level, mobName, health, maxHealth);

        entity.customName(ChatUtils.format(formattedName));
        entity.setCustomNameVisible(true);
    }
}