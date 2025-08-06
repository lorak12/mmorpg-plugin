package org.nakii.mmorpg.managers;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.shapers.ParticleSphere;
import hm.zelha.particlesfx.util.ParticleSFX;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.entity.CustomMob;
import org.nakii.mmorpg.entity.ability.Ability;
import org.nakii.mmorpg.entity.ability.TriggerType;
import org.nakii.mmorpg.utils.ChatUtils;
//import org.nakii.mmorpg.utils.EffectFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class AbilityManager {


    private final MMORPGCore plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>(); // Mob UUID -> Ability ID -> Cooldown End Time

    public AbilityManager(MMORPGCore plugin) {
        this.plugin = plugin;
        startTimerTask();
    }

    public void handleTrigger(TriggerType trigger, LivingEntity mob, LivingEntity target) {
        String mobId = plugin.getMobManager().getMobId(mob);
        if (mobId == null) return;

        CustomMob customMob = plugin.getMobManager().getCustomMob(mobId);
        if (customMob == null || customMob.getConfig().getConfigurationSection("abilities") == null) return;

        for (String abilityId : customMob.getConfig().getConfigurationSection("abilities").getKeys(false)) {
            Ability ability = new Ability(abilityId, customMob.getConfig().getConfigurationSection("abilities." + abilityId));
            if (ability.getTrigger() == trigger) {
                if (checkConditions(ability, mob, target)) {
                    executeActions(ability, mob, target);
                }
            }
        }
    }

    private boolean checkConditions(Ability ability, LivingEntity mob, LivingEntity target) {
        List<Map<?, ?>> conditionMaps = ability.getConfig().getMapList("conditions");
        if (conditionMaps.isEmpty()) {
            return true; // No conditions, so the ability is always allowed to run.
        }

        for (Map<?, ?> conditionMap : conditionMaps) {
            // Convert the map from the list into a temporary ConfigurationSection
            ConfigurationSection condition = new MemoryConfiguration();
            conditionMap.forEach((key, value) -> condition.set(key.toString(), value));

            String type = condition.getString("type", "").toUpperCase();
            if (type.isEmpty()) {
                continue; // Skip invalid condition entries
            }

            // --- Perform the actual condition checks ---
            boolean conditionPassed = true;
            switch (type) {
                case "CHANCE":
                    if (ThreadLocalRandom.current().nextDouble() > condition.getDouble("value", 1.0)) {
                        conditionPassed = false;
                    }
                    break;
                case "HEALTH_IS_BELOW":
                    // Use the custom HealthManager to get the real health percentage
                    double currentHealth = plugin.getHealthManager().getCurrentHealth(mob);
                    double maxHealth = plugin.getHealthManager().getMaxHealth(mob);
                    double healthPercent = (maxHealth > 0) ? currentHealth / maxHealth : 0;
                    if (healthPercent > condition.getDouble("value")) {
                        conditionPassed = false;
                    }
                    break;
                case "COOLDOWN":
                    long now = System.currentTimeMillis();
                    long endTime = cooldowns.computeIfAbsent(mob.getUniqueId(), k -> new HashMap<>())
                            .getOrDefault(ability.getId(), 0L);
                    if (now < endTime) {
                        conditionPassed = false; // Still on cooldown
                    }
                    break;
            }

            // If any single condition fails, the ability cannot run.
            if (!conditionPassed) {
                return false;
            }
        }

        // If the loop completes, it means all conditions passed.
        return true;
    }

    // New executeActions method for AbilityManager.java
    // Replace your current executeActions method with this one
    private void executeActions(Ability ability, LivingEntity mob, LivingEntity target) {
        // --- BEGIN NEW COOLDOWN LOGIC ---
        // After an ability's conditions have passed, check if one of those conditions was a cooldown.
        // If so, put the ability on cooldown now.
        List<Map<?, ?>> conditionMaps = ability.getConfig().getMapList("conditions");
        if (!conditionMaps.isEmpty()) {
            for (Map<?, ?> rawMap : conditionMaps) {
                @SuppressWarnings("unchecked")
                Map<String, Object> conditionMap = (Map<String, Object>) rawMap;

                String type = String.valueOf(conditionMap.getOrDefault("type", "")).toUpperCase();
                if ("COOLDOWN".equals(type)) {
                    long seconds = Long.parseLong(String.valueOf(conditionMap.getOrDefault("seconds", 5)));
                    long cooldownMillis = seconds * 1000;

                    cooldowns.computeIfAbsent(mob.getUniqueId(), k -> new HashMap<>())
                            .put(ability.getId(), System.currentTimeMillis() + cooldownMillis);

                    break; // A single ability only needs one cooldown.
                }
            }
        }
        // --- END NEW COOLDOWN LOGIC ---

        // Read the "actions" block as a List of Maps.
        List<Map<?, ?>> actionMaps = ability.getConfig().getMapList("actions");
        if (actionMaps.isEmpty()) {
            return; // Exit if there are no actions defined.
        }

        for (Map<?, ?> rawActionMap : actionMaps) {
            @SuppressWarnings("unchecked")
            Map<String, Object> actionMap = (Map<String, Object>) rawActionMap;

            // Convert the Map into a temporary ConfigurationSection.
            ConfigurationSection actionSection = new MemoryConfiguration();
            actionMap.forEach(actionSection::set);

            // Schedule the action to be performed, respecting its individual delay.
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (mob.isValid()) {
                        performAction(actionSection, mob, target);
                    }
                }
            }.runTaskLater(plugin, actionSection.getLong("delay", 0L));
        }
    }

    private void performAction(ConfigurationSection action, LivingEntity mob, LivingEntity target) {
        String type = action.getString("type", "").toUpperCase();
        String targetSelector = action.getString("target", "SELF").toUpperCase();
        LivingEntity actionTarget = "ATTACK_TARGET".equals(targetSelector) ? target : mob;

        switch (type) {
            case "MESSAGE":
                String text = action.getString("text", "");
                if (text.isEmpty()) break;

                // NEW, CORRECT WAY:
                if ("NEARBY_PLAYERS".equals(targetSelector)) {
                    double radius = action.getDouble("radius", 30);
                    mob.getNearbyEntities(radius, radius, radius).stream()
                            .filter(e -> e instanceof Player)
                            .forEach(p -> p.sendMessage(ChatUtils.format(text)));
                } else if (actionTarget instanceof Player) {
                    actionTarget.sendMessage(ChatUtils.format(text));
                }
                break;
            case "POTION":
                PotionEffectType effectType = PotionEffectType.getByName(action.getString("potion_effect"));
                if (effectType != null && actionTarget != null) {
                    actionTarget.addPotionEffect(new PotionEffect(effectType, action.getInt("duration", 100), action.getInt("amplifier", 0)));
                }
                break;
            case "SUMMON":
                for (int i = 0; i < action.getInt("amount", 1); i++) {
                    plugin.getMobManager().spawnMob(action.getString("mob_id"), mob.getLocation());
                }
                break;
            case "SOUND":
                mob.getWorld().playSound(mob.getLocation(), Sound.valueOf(action.getString("sound")), 1.0f, 1.0f);
                break;
            case "DAMAGE":
                double radius = action.getDouble("radius", 5.0);
                double amount = action.getDouble("amount", 10.0);
                mob.getNearbyEntities(radius, radius, radius).forEach(entity -> {
                    if (entity instanceof Player) {
                        ((Player) entity).damage(amount, mob);
                    }
                });
                break;


        }
    }

    private void startTimerTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (LivingEntity entity : world.getLivingEntities()) {
                    if (plugin.getMobManager().isCustomMob(entity)) {
                        handleTrigger(TriggerType.TIMER, entity, null);
                    }
                }
            }
        }, 20L, 20L); // Run every second
    }
}