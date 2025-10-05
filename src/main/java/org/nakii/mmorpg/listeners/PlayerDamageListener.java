package org.nakii.mmorpg.listeners;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.enchantment.effects.EnchantmentEffect;
import org.nakii.mmorpg.managers.*;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.slayer.ActiveSlayerQuest;
import org.nakii.mmorpg.slayer.PlayerSlayerData;
import org.nakii.mmorpg.util.VectorUtils;

import java.util.List;
import java.util.Map;

public class PlayerDamageListener implements Listener {

    private final MMORPGCore plugin;
    private final CombatTracker combatTracker;
    private final EnchantmentManager enchantmentManager;
    private final StatsManager statsManager;
    private final DamageManager damageManager;

    public PlayerDamageListener(MMORPGCore plugin) {
        this.plugin = plugin;
        this.combatTracker = plugin.getCombatTracker();
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.statsManager = plugin.getStatsManager();
        this.damageManager = plugin.getDamageManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // --- NEW: Check for the Bypass Defense flag ---
        boolean bypassDefense = victim.hasMetadata(DamageManager.BYPASS_DEFENSE_META_KEY);
        if (bypassDefense) {
            // It's important to remove the metadata so it doesn't affect subsequent hits
            victim.removeMetadata(DamageManager.BYPASS_DEFENSE_META_KEY, plugin);
        }

        double finalDamage = event.getDamage();
        boolean isCustomCrit = false;

        // If the damage is from our ability, we don't need to recalculate it.
        // We only run these blocks for standard weapon/mob attacks.
        if (!bypassDefense) {
            // Neutralize vanilla critical damage
            if (event.isCritical()) {
                finalDamage /= 1.5;
            }

            // --- Phase 1: Determine Attacker's Base Damage ---
            if (event.getDamager() instanceof Player attacker) {
                PlayerStats attackerStats = statsManager.getStats(attacker);
                isCustomCrit = (Math.random() * 100 < attackerStats.getCritChance());

                // Calculate full outgoing damage, including stats and offensive enchants
                finalDamage = damageManager.calculatePlayerDamage(attacker, victim, finalDamage, isCustomCrit);

                ItemStack weapon = attacker.getInventory().getItemInMainHand();

                // --- NEW: Passive Effect Logic ---
                if (weapon != null && weapon.hasItemMeta()) {
                    // Example: Livid Dagger passive
                    String itemId = weapon.getItemMeta().getPersistentDataContainer().get(ItemManager.ITEM_ID_KEY, PersistentDataType.STRING);
                    if ("LIVID_DAGGER".equals(itemId)) {
                        if (VectorUtils.isBehind(attacker, victim)) {
                            // Apply 100% more damage if it's a critical hit
                            if (isCustomCrit) {
                                finalDamage *= 2;
                            }
                        }
                    }
                }


                // Apply damage-modifying enchantments
                if (weapon != null && !weapon.getType().isAir()) {
                    Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
                    for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                        CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                        if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                        EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
                        if (effect != null) {
                            finalDamage = effect.onDamageModify(finalDamage, event, enchantment, entry.getValue(), isCustomCrit);
                        }
                    }
                }
            }
            // 1b: --- THIS IS THE NEW LOGIC BLOCK ---
            // If the attacker is a MOB
            else if (event.getDamager() instanceof LivingEntity mobAttacker) {
                // Check if it's one of our custom mobs
                if (plugin.getMobManager().isCustomMob(mobAttacker)) {
                    CustomMobTemplate template = plugin.getMobManager().getTemplate(plugin.getMobManager().getMobId(mobAttacker));
                    if (template != null) {
                        // Set the event's base damage to our custom mob's damage stat.
                        // In the future, you could add a formula here for mob Strength, etc.
                        finalDamage = template.getStat(Stat.DAMAGE);
                    }
                }
            }
        }

        // --- Phase 2: Victim Logic (Apply Defenses) ---
        finalDamage = damageManager.applyDefense(victim, finalDamage);

        // We attach a temporary piece of metadata to the entity that was hit.
        // This metadata will be readable by our lower-priority listener.
        // The key is a unique string, and the value is the boolean result.
        event.getEntity().setMetadata("mmorpg_last_hit_crit", new FixedMetadataValue(plugin, isCustomCrit));

        // --- Finalization ---
        event.setDamage(Math.floor(finalDamage));

        // --- Phase 3: Post-Hit Triggers ---
        // (This happens after the final damage value has been set)

        // 3a: Trigger Attacker's onAttack effects
        if (event.getDamager() instanceof Player attacker) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (weapon != null && !weapon.getType().isAir()) {
                Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                    if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                    EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
                    if (effect != null) {
                        effect.onAttack(event, enchantment, entry.getValue(), isCustomCrit);
                    }
                }
            }
            combatTracker.recordHit(attacker, victim);
        }

        // 3b: Trigger Victim's onDamaged effects (if victim is a Player)
        if (victim instanceof Player victimPlayer) {
            for (ItemStack armorPiece : victimPlayer.getInventory().getArmorContents()) {
                if (armorPiece == null || armorPiece.getType().isAir()) continue;
                Map<String, Integer> enchantments = enchantmentManager.getEnchantments(armorPiece);
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                    if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                    EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
                    if (effect != null) {
                        effect.onDamaged(event, enchantment, entry.getValue());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        LivingEntity victim = event.getEntity();
        SlayerManager slayerManager = plugin.getSlayerManager();
        String mobId = plugin.getMobManager().getMobId(victim);

        // --- 1. Handle Slayer Boss Death Logic ---
        if (mobId != null) {
            ActiveSlayerQuest quest = slayerManager.getActiveSlayerQuest(killer);
            if (quest != null && quest.getState() == ActiveSlayerQuest.QuestState.BOSS_FIGHT) {
                if (victim.getUniqueId().equals(quest.getActiveBoss().getUniqueId())) {
                    // It's the player's quest boss, update the quest state.
                    quest.setState(ActiveSlayerQuest.QuestState.AWAITING_CLAIM);
                    quest.setActiveBoss(null);
                    plugin.getScoreboardManager().updateScoreboard(killer);
                    killer.sendMessage(MMORPGCore.getInstance().getMiniMessage().deserialize("<green><b>SLAYER BOSS SLAIN!</b></green> <gray>Visit Maddox to claim your rewards.</gray>"));
                    killer.playSound(killer.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.2f);

                    // Record that the player has now defeated this tier.
                    PlayerSlayerData slayerData = plugin.getSlayerDataManager().getData(killer);
                    if (slayerData != null) {
                        slayerData.setHighestTierDefeated(quest.getSlayerType(), quest.getTier());
                    }

                    // Grant Slayer Leveling XP.
                    ConfigurationSection bossConfig = slayerManager.getBossConfigById(mobId);
                    if (bossConfig != null) {
                        int xpReward = bossConfig.getInt("slayer-xp-reward", 0);
                        slayerManager.addSlayerExperience(killer, quest.getSlayerType(), xpReward);
                    }
                }
            }
        }

        // --- 2. Handle Loot for all Custom Mobs (Bosses and regular) ---
        if (mobId != null) {
            // Take full manual control of drops.
            event.getDrops().clear();
            event.setDroppedExp(0);

            List<ItemStack> finalLoot;
            ConfigurationSection bossConfig = slayerManager.getBossConfigById(mobId);

            if (bossConfig != null) {
                finalLoot = plugin.getLootManager().rollSlayerLoot(killer, bossConfig);
            } else {
                finalLoot = plugin.getLootManager().rollLootTable(killer, mobId);
            }

            // --- THIS IS THE CORRECTED DROP LOGIC ---
            // Iterate the CALCULATED loot, not the event's empty drop list.
            for (ItemStack drop : finalLoot) {
                String collectionId = plugin.getCollectionManager().getCollectionId(drop.getType());
                if (collectionId != null) {
                    plugin.getCollectionManager().addProgress(killer, collectionId, drop.getAmount());
                }
                // Use the pristine dropper for every single item.
                org.nakii.mmorpg.util.ItemDropper.dropPristineItem(killer, victim.getLocation(), drop);
            }
        }

        // --- 3. Handle Combat XP (Runs for ALL mobs, custom or vanilla) ---
        plugin.getSkillManager().handleMobKill(killer, victim);

        // --- 4. Handle onKill Enchantment Effects (Runs for ALL mobs) ---
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir()) return;

        Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
        if (enchantments.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
            if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
            EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
            if (effect != null) {
                effect.onKill(event, killer, enchantment, entry.getValue());
            }
        }
    }
}