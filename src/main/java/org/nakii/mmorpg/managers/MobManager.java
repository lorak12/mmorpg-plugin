package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
import org.nakii.mmorpg.utils.ChatUtils;

import java.io.File;
import java.util.*;

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

    /**
     * The main mob spawning logic. Spawns a custom mob and allows for its base
     * stats to be overridden by a ConfigurationSection, which is essential for Slayer bosses.
     *
     * @param mobId         The ID of the mob template to use.
     * @param location      The location to spawn the mob at.
     * @param statOverrides A ConfigurationSection (e.g., from slayers.yml) containing override stats. Can be null.
     * @return The spawned LivingEntity, or null if spawning failed.
     */
    public LivingEntity spawnMob(String mobId, Location location, @Nullable ConfigurationSection statOverrides) {
        CustomMobTemplate template = getTemplate(mobId);
        if (template == null) {
            plugin.getLogger().warning("Attempted to spawn unknown mob with ID: " + mobId);
            return null;
        }

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, template.getEntityType());
        var data = entity.getPersistentDataContainer();

        // --- 1. Store Core Identifiers in NBT ---
        data.set(MOB_ID_KEY, PersistentDataType.STRING, template.getId());
        data.set(MOB_CATEGORY_KEY, PersistentDataType.STRING, template.getMobCategory());

        // --- 2. Calculate and Store All Stats in NBT ---
        // This loop iterates through every stat defined in your Stat enum.
        // It prioritizes the override value but falls back to the template's default.
        for (Stat stat : Stat.values()) {
            // Stat names in config are lowercase (e.g., "health", "damage")
            String configKey = stat.name().toLowerCase();

            // Get the base value from the template
            double baseValue = template.getStat(stat);

            // Check for an override and apply it if it exists
            double finalValue = (statOverrides != null)
                    ? statOverrides.getDouble(configKey, baseValue)
                    : baseValue;

            // If the final value is not the default (0), store it in the mob's NBT.
            // This is where all custom stats like DAMAGE, DEFENSE, STRENGTH etc. are saved
            // for your DamageManager to read later.
            if (finalValue != 0) {
                // You'll need a NamespacedKey for each stat. A helper method is best.
                data.set(getStatKey(stat), PersistentDataType.DOUBLE, finalValue);
            }
        }

        // --- 3. Apply Core Vanilla Attributes ---
        // Some stats directly affect Bukkit attributes. We apply them here.
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
                // This assumes your SPEED stat is a multiplier on the vanilla base.
                // You may need to adjust this formula based on your game design.
                speedAttr.setBaseValue(speedAttr.getDefaultValue() * (movementSpeed / 100.0));
            }
        }

        // --- 4. Apply Equipment ---
        template.getEquipment().ifPresent(equipmentMap -> {
            EntityEquipment equipment = entity.getEquipment();
            if (equipmentMap.containsKey("hand"))
                equipment.setItemInMainHand(plugin.getItemManager().createItemStack(equipmentMap.get("hand")));
            if (equipmentMap.containsKey("offhand"))
                equipment.setItemInOffHand(plugin.getItemManager().createItemStack(equipmentMap.get("offhand")));
            if (equipmentMap.containsKey("helmet"))
                equipment.setHelmet(plugin.getItemManager().createItemStack(equipmentMap.get("helmet")));
            if (equipmentMap.containsKey("chestplate"))
                equipment.setChestplate(plugin.getItemManager().createItemStack(equipmentMap.get("chestplate")));
            if (equipmentMap.containsKey("leggings"))
                equipment.setLeggings(plugin.getItemManager().createItemStack(equipmentMap.get("leggings")));
            if (equipmentMap.containsKey("boots"))
                equipment.setBoots(plugin.getItemManager().createItemStack(equipmentMap.get("boots")));
        });

        // --- 5. Finalize ---
        updateHealthDisplay(entity);
        return entity;
    }

    // --- ADD THESE TWO HELPER METHODS TO YOUR MOBMANAGER CLASS ---

    /**
     * Creates a standardized NamespacedKey for a given stat.
     * @param stat The stat to create a key for.
     * @return The NamespacedKey, e.g., "mmorpg:stat_health".
     */
    private NamespacedKey getStatKey(Stat stat) {
        return new NamespacedKey(plugin, "stat_" + stat.name().toLowerCase());
    }

    /**
     * A helper to read a stat value back from a PersistentDataContainer.
     * @param data The container to read from.
     * @param stat The stat to retrieve.
     * @return The value of the stat, or 0 if not present.
     */
    private double getStatFromNBT(PersistentDataContainer data, Stat stat) {
        return data.getOrDefault(getStatKey(stat), PersistentDataType.DOUBLE, 0.0);
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
     * Retrieves a CustomMob data object from the cache by its unique ID.
     * @param id The ID of the mob to retrieve.
     * @return The CustomMob object, or null if no mob with that ID exists.
     */
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

    /**
     * Finds all currently loaded LivingEntities that are identified as Slayer Bosses.
     * @return A list of active boss entities.
     */
    public List<LivingEntity> getActiveSlayerBosses() {
        List<LivingEntity> bosses = new ArrayList<>();
        // This is a simple implementation. For a large server, you'd want to optimize this.
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (isSlayerBoss(entity)) { // We'll create this helper method next
                    bosses.add(entity);
                }
            }
        }
        return bosses;
    }

    public boolean isSlayerBoss(LivingEntity entity) {
        String mobId = getMobId(entity);
        if (mobId == null) return false;
        // This checks if the mob's ID exists as a boss ID in the slayers.yml
        return plugin.getSlayerManager().isSlayerBossId(mobId);
    }

    public List<String> getBossAbilities(LivingEntity entity) {
        String mobId = getMobId(entity);
        if (mobId == null) return Collections.emptyList();
        // This gets the list of abilities from slayers.yml
        return plugin.getSlayerManager().getBossAbilitiesById(mobId);
    }

    /**
     * Reads the mob's unique ID from its PersistentDataContainer (NBT).
     * @param entity The entity to check.
     * @return The mob's ID as a String, or null if it's not a custom mob.
     */
    @Nullable
    public String getMobId(LivingEntity entity) {
        if (entity == null) return null;
        return entity.getPersistentDataContainer().get(MOB_ID_KEY, PersistentDataType.STRING);
    }

    /**
     * Checks if an entity is a custom mob by looking for the MOB_ID_KEY in its NBT.
     * @param entity The entity to check.
     * @return True if it is a custom mob, false otherwise.
     */
    public boolean isCustomMob(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getPersistentDataContainer().has(MOB_ID_KEY, PersistentDataType.STRING);
    }

    /**
     * Retrieves a CustomMobTemplate from the cache.
     * @param mobId The ID of the mob template.
     * @return The template, or null if not found.
     */
    @Nullable
    public CustomMobTemplate getTemplate(String mobId) {
        if (mobId == null) return null;
        return mobRegistry.get(mobId);
    }
}