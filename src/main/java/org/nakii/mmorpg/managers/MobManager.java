package org.nakii.mmorpg.managers;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.util.ChatUtils;
import org.nakii.mmorpg.util.Keys;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MobManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomMobTemplate> mobRegistry = new HashMap<>();
    private final ItemManager itemManager;

    // The SlayerManager dependency has been removed.
    public MobManager(MMORPGCore plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        loadMobs();
    }

    public void loadMobs() {
        mobRegistry.clear();
        File mobsFolder = new File(plugin.getDataFolder(), "mobs");
        if (!mobsFolder.exists()) mobsFolder.mkdirs();
        loadMobsFromDirectory(mobsFolder);
        plugin.getLogger().info("Loaded " + mobRegistry.size() + " custom mob templates.");
    }

    private void loadMobsFromDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                loadMobsFromDirectory(file);
            } else if (file.getName().endsWith(".yml")) {
                var config = YamlConfiguration.loadConfiguration(file);
                for (String key : config.getKeys(false)) {
                    ConfigurationSection mobSection = config.getConfigurationSection(key);
                    if (mobSection != null) {
                        mobRegistry.put(key.toUpperCase(), new CustomMobTemplate(key.toUpperCase(), mobSection));
                    }
                }
            }
        }
    }

    public LivingEntity spawnMob(String mobId, Location location, @Nullable ConfigurationSection statOverrides) {
        CustomMobTemplate template = getTemplate(mobId);
        if (template == null) {
            plugin.getLogger().warning("Attempted to spawn unknown mob with ID: " + mobId);
            return null;
        }

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, template.getEntityType());
        var data = entity.getPersistentDataContainer();

        data.set(Keys.MOB_ID, PersistentDataType.STRING, template.getId());
        data.set(Keys.MOB_CATEGORY, PersistentDataType.STRING, template.getMobCategory());

        for (Stat stat : Stat.values()) {
            String configKey = stat.name().toLowerCase();
            double baseValue = template.getStat(stat);
            double finalValue = (statOverrides != null) ? statOverrides.getDouble(configKey, baseValue) : baseValue;
            if (finalValue != 0) {
                data.set(Keys.mobStatKey(stat), PersistentDataType.DOUBLE, finalValue);
            }
        }

        double maxHealth = getStatFromNBT(data, Stat.HEALTH);
        if (maxHealth > 0) {
            AttributeInstance healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(maxHealth);
                entity.setHealth(maxHealth);
            }
        }

        double movementSpeed = getStatFromNBT(data, Stat.SPEED);
        if (movementSpeed > 0) {
            AttributeInstance speedAttr = entity.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.setBaseValue(speedAttr.getDefaultValue() * (movementSpeed / 100.0));
            }
        }

        template.getEquipment().ifPresent(equipmentMap -> {
            EntityEquipment equipment = entity.getEquipment();
            if (equipmentMap.containsKey("hand"))
                equipment.setItemInMainHand(itemManager.createItemStack(equipmentMap.get("hand")));
            if (equipmentMap.containsKey("offhand"))
                equipment.setItemInOffHand(itemManager.createItemStack(equipmentMap.get("offhand")));
            if (equipmentMap.containsKey("helmet"))
                equipment.setHelmet(itemManager.createItemStack(equipmentMap.get("helmet")));
            if (equipmentMap.containsKey("chestplate"))
                equipment.setChestplate(itemManager.createItemStack(equipmentMap.get("chestplate")));
            if (equipmentMap.containsKey("leggings"))
                equipment.setLeggings(itemManager.createItemStack(equipmentMap.get("leggings")));
            if (equipmentMap.containsKey("boots"))
                equipment.setBoots(itemManager.createItemStack(equipmentMap.get("boots")));
        });

        updateHealthDisplay(entity);
        return entity;
    }

    private double getStatFromNBT(PersistentDataContainer data, Stat stat) {
        return data.getOrDefault(Keys.mobStatKey(stat), PersistentDataType.DOUBLE, 0.0);
    }

    public String getMobCategory(LivingEntity entity) {
        if (entity == null || !entity.getPersistentDataContainer().has(Keys.MOB_CATEGORY, PersistentDataType.STRING)) {
            return null;
        }
        return entity.getPersistentDataContainer().get(Keys.MOB_CATEGORY, PersistentDataType.STRING);
    }

    public Map<String, CustomMobTemplate> getMobRegistry(){
        return mobRegistry;
    }

    @Nullable
    public CustomMobTemplate getMobById(String id) {
        return mobRegistry.get(id);
    }

    public void updateHealthDisplay(LivingEntity entity) {
        CustomMobTemplate template = getTemplate(getMobId(entity));
        if (template == null) return;

        String formattedName = String.format("<gray>[</gray><white>Lv%d</white><gray>]</gray> %s <green>%.0f</green><white>/</white><green>%.0f</green><red>❤</red>",
                template.getLevel(), template.getDisplayName(), entity.getHealth(), entity.getMaxHealth());

        entity.customName(ChatUtils.format(formattedName));
        entity.setCustomNameVisible(true);
    }

    @Nullable
    public String getMobId(LivingEntity entity) {
        if (entity == null) return null;
        return entity.getPersistentDataContainer().get(Keys.MOB_ID, PersistentDataType.STRING);
    }

    public boolean isCustomMob(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getPersistentDataContainer().has(Keys.MOB_ID, PersistentDataType.STRING);
    }

    @Nullable
    public CustomMobTemplate getTemplate(String mobId) {
        if (mobId == null) return null;
        return mobRegistry.get(mobId);
    }
}