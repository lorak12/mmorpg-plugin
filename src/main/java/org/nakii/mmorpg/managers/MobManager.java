package org.nakii.mmorpg.managers;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.utils.ChatUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MobManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomMobTemplate> mobRegistry = new HashMap<>();

    public static final NamespacedKey MOB_ID_KEY = new NamespacedKey(MMORPGCore.getInstance(), "mob_id");
    public static final NamespacedKey MOB_CATEGORY_KEY = new NamespacedKey(MMORPGCore.getInstance(), "mob_category");

    public MobManager(MMORPGCore plugin) {
        this.plugin = plugin;
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
                loadMobsFromDirectory(file); // Recursively load
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

    public CustomMobTemplate getTemplate(String mobId) {
        // Add a guard clause at the beginning. If the provided mobId is null,
        // we can't possibly find a template for it, so we return null immediately.
        if (mobId == null) {
            return null;
        }
        return mobRegistry.get(mobId.toUpperCase());
    }

    public LivingEntity spawnMob(String mobId, Location location) {
        CustomMobTemplate template = getTemplate(mobId);
        if (template == null) {
            plugin.getLogger().warning("Attempted to spawn unknown mob with ID: " + mobId);
            return null;
        }

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, template.getEntityType());

        // Store custom data in NBT
        var data = entity.getPersistentDataContainer();
        data.set(MOB_ID_KEY, PersistentDataType.STRING, template.getId());
        data.set(MOB_CATEGORY_KEY, PersistentDataType.STRING, template.getMobCategory());

        // Apply stats
        double maxHealth = template.getStat(Stat.HEALTH);
        if (maxHealth > 0) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            entity.setHealth(maxHealth);
        }

        // Apply equipment
        template.getEquipment().ifPresent(equipmentMap -> {
            EntityEquipment equipment = entity.getEquipment();
            if (equipmentMap.containsKey("hand"))
                equipment.setItemInMainHand(plugin.getItemManager().createItemStack(equipmentMap.get("hand")));
            if (equipmentMap.containsKey("offhand"))
                equipment.setItemInOffHand(plugin.getItemManager().createItemStack(equipmentMap.get("offhand")));
            if (equipmentMap.containsKey("helmet"))
                equipment.setHelmet(plugin.getItemManager().createItemStack(equipmentMap.get("helmet")));
            if (equipmentMap.containsKey("chestplate"))
                equipment.setHelmet(plugin.getItemManager().createItemStack(equipmentMap.get("chestplate")));
            if (equipmentMap.containsKey("leggings"))
                equipment.setHelmet(plugin.getItemManager().createItemStack(equipmentMap.get("leggings")));
            if (equipmentMap.containsKey("boots"))
                equipment.setHelmet(plugin.getItemManager().createItemStack(equipmentMap.get("boots")));

        });

        // Set the display name
        updateHealthDisplay(entity);
        return entity;
    }

    public String getMobId(LivingEntity entity) {
        if (entity == null || !entity.getPersistentDataContainer().has(MOB_ID_KEY, PersistentDataType.STRING)) {
            return null;
        }
        return entity.getPersistentDataContainer().get(MOB_ID_KEY, PersistentDataType.STRING);
    }

    public String getMobCategory(LivingEntity entity) {
        if (entity == null || !entity.getPersistentDataContainer().has(MOB_CATEGORY_KEY, PersistentDataType.STRING)) {
            return null;
        }
        return entity.getPersistentDataContainer().get(MOB_CATEGORY_KEY, PersistentDataType.STRING);
    }

    public Map<String, CustomMobTemplate> getMobRegistry(){
        return mobRegistry;
    }

    /**
     * Checks if a given entity is a custom mob managed by this system.
     * @param entity The entity to check.
     * @return True if it's a custom mob, false otherwise.
     */
    public boolean isCustomMob(LivingEntity entity) {
        return getMobId(entity) != null;
    }

    /**
     * Gets the level of a custom mob.
     * @param entity The custom mob.
     * @return The mob's level, or 0 if it's not a valid custom mob.
     */
    public int getMobLevel(LivingEntity entity) {
        if (!isCustomMob(entity)) return 0;
        CustomMobTemplate template = getTemplate(getMobId(entity));
        return (template != null) ? template.getLevel() : 0;
    }


    public void updateHealthDisplay(LivingEntity entity) {
        CustomMobTemplate template = getTemplate(getMobId(entity));
        if (template == null) return;

        String formattedName = String.format("<gray>[</gray><white>Lv%d</white><gray>]</gray> %s <green>%.0f</green><white>/</white><green>%.0f</green><red>❤</red>",
                template.getLevel(), template.getDisplayName(), entity.getHealth(), entity.getMaxHealth());

        entity.customName(ChatUtils.format(formattedName));
        entity.setCustomNameVisible(true);
    }
}