package org.nakii.mmorpg.managers;

import de.slikey.effectlib.Effect;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.entity.CustomMob;
import org.nakii.mmorpg.entity.ability.Ability;
import org.nakii.mmorpg.entity.ability.TriggerType;

import java.util.Collection;
import java.util.HashMap;
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
        ConfigurationSection conditions = ability.getConditionsConfig();
        if (conditions == null) return true; // No conditions, always pass

        for (String key : conditions.getKeys(false)) {
            ConfigurationSection condition = conditions.getConfigurationSection(key);
            String type = condition.getString("type", "").toUpperCase();

            switch (type) {
                case "CHANCE":
                    if (ThreadLocalRandom.current().nextDouble() > condition.getDouble("value", 1.0)) return false;
                    break;
                case "HEALTH_IS_BELOW":
                    double healthPercent = mob.getHealth() / mob.getMaxHealth();
                    if (healthPercent > condition.getDouble("value")) return false;
                    break;
                case "COOLDOWN":
                    long now = System.currentTimeMillis();
                    long endTime = cooldowns.computeIfAbsent(mob.getUniqueId(), k -> new HashMap<>())
                            .getOrDefault(ability.getId(), 0L);
                    if (now < endTime) return false; // Still on cooldown
                    break;
            }
        }
        return true;
    }

    private void executeActions(Ability ability, LivingEntity mob, LivingEntity target) {
        ConfigurationSection actions = ability.getActionsConfig();
        if (actions == null) return;

        // Set cooldown if applicable
        if (ability.getConfig().getConfigurationSection("conditions") != null) {
            ability.getConfig().getConfigurationSection("conditions").getKeys(false).forEach(key -> {
                ConfigurationSection condition = ability.getConfig().getConfigurationSection("conditions." + key);
                if ("COOLDOWN".equalsIgnoreCase(condition.getString("type"))) {
                    long cooldownMillis = condition.getLong("seconds", 5) * 1000;
                    cooldowns.computeIfAbsent(mob.getUniqueId(), k -> new HashMap<>())
                            .put(ability.getId(), System.currentTimeMillis() + cooldownMillis);
                }
            });
        }

        for (String key : actions.getKeys(false)) {
            ConfigurationSection action = actions.getConfigurationSection(key);
            new BukkitRunnable() {
                @Override
                public void run() {
                    performAction(action, mob, target);
                }
            }.runTaskLater(plugin, action.getLong("delay", 0L));
        }
    }

    private void performAction(ConfigurationSection action, LivingEntity mob, LivingEntity target) {
        String type = action.getString("type", "").toUpperCase();
        String targetSelector = action.getString("target", "SELF").toUpperCase();
        LivingEntity actionTarget = "ATTACK_TARGET".equals(targetSelector) ? target : mob;

        switch (type) {
            case "MESSAGE":
                if (actionTarget instanceof Player) {
                    actionTarget.sendMessage(ChatColor.translateAlternateColorCodes('&', action.getString("text", "")));
                } else if ("NEARBY_PLAYERS".equals(targetSelector)) {
                    double radius = action.getDouble("radius", 30);
                    mob.getNearbyEntities(radius, radius, radius).forEach(entity -> {
                        if (entity instanceof Player) {
                            entity.sendMessage(ChatColor.translateAlternateColorCodes('&', action.getString("text", "")));
                        }
                    });
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
            case "EFFECTLIB_PARTICLE":
                // BUG FIX: Use the correct method signature as required by the new API version.
                if (plugin.getEffectManager() != null && actionTarget != null) {
                    String effectName = action.getString("effect", "vortex");
                    // We must provide a ConfigurationSection, even if it's empty.
                    ConfigurationSection effectConfig = new YamlConfiguration();

                    // The simplest available method is start(name, config, location, targetEntity)
                    plugin.getEffectManager().start(effectName, effectConfig, actionTarget.getEyeLocation(), null, null, null, null);
                }
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