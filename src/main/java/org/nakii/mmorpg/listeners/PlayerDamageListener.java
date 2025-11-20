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
import org.nakii.mmorpg.util.Keys;
import org.nakii.mmorpg.util.VectorUtils;

import java.util.List;
import java.util.Map;

public class PlayerDamageListener implements Listener {

    private final MMORPGCore plugin;
    private final CombatTracker combatTracker;
    private final EnchantmentManager enchantmentManager;
    private final EnchantmentEffectManager enchantmentEffectManager;
    private final StatsManager statsManager;
    private final DamageManager damageManager;
    private final MobManager mobManager;
    private final SlayerManager slayerManager;
    private final SlayerDataManager slayerDataManager;
    private final ScoreboardManager scoreboardManager;
    private final LootManager lootManager;
    private final CollectionManager collectionManager;
    private final SkillManager skillManager;
    private final PlayerManager playerManager; // New dependency
    private final HUDManager hudManager;       // New dependency

    public PlayerDamageListener(MMORPGCore plugin, CombatTracker combatTracker, EnchantmentManager enchantmentManager,
                                EnchantmentEffectManager enchantmentEffectManager, StatsManager statsManager,
                                DamageManager damageManager, MobManager mobManager, SlayerManager slayerManager,
                                SlayerDataManager slayerDataManager, ScoreboardManager scoreboardManager,
                                LootManager lootManager, CollectionManager collectionManager, SkillManager skillManager,
                                PlayerManager playerManager, HUDManager hudManager) { // Updated constructor
        this.plugin = plugin;
        this.combatTracker = combatTracker;
        this.enchantmentManager = enchantmentManager;
        this.enchantmentEffectManager = enchantmentEffectManager;
        this.statsManager = statsManager;
        this.damageManager = damageManager;
        this.mobManager = mobManager;
        this.slayerManager = slayerManager;
        this.slayerDataManager = slayerDataManager;
        this.scoreboardManager = scoreboardManager;
        this.lootManager = lootManager;
        this.collectionManager = collectionManager;
        this.skillManager = skillManager;
        this.playerManager = playerManager; // New dependency
        this.hudManager = hudManager;       // New dependency
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Tag both attacker and victim as "in-combat" at the start of any fight.
        if (event.getDamager() instanceof Player attacker) {
            combatTracker.tag(attacker);
        }
        if (victim instanceof Player victimPlayer) {
            combatTracker.tag(victimPlayer);
        }

        boolean bypassDefense = victim.hasMetadata(Keys.BYPASS_DEFENSE.getKey());
        if (bypassDefense) {
            victim.removeMetadata(Keys.BYPASS_DEFENSE.getKey(), plugin);
        }

        double finalDamage = event.getDamage();
        boolean isCustomCrit = false;

        if (!bypassDefense) {
            if (event.isCritical()) {
                finalDamage /= 1.5; // Remove vanilla crit bonus to apply our own
            }

            if (event.getDamager() instanceof Player attacker) {
                PlayerStats attackerStats = statsManager.getStats(attacker);
                isCustomCrit = (Math.random() * 100 < attackerStats.getCritChance());

                // Gather special conditions (context) for the damage calculation
                boolean isBackstab = false;
                ItemStack weapon = attacker.getInventory().getItemInMainHand();
                if (weapon != null && weapon.hasItemMeta()) {
                    String itemId = weapon.getItemMeta().getPersistentDataContainer().get(Keys.ITEM_ID, PersistentDataType.STRING);
                    if ("LIVID_DAGGER".equals(itemId) && VectorUtils.isBehind(attacker, victim)) {
                        isBackstab = true;
                    }
                }

                // Delegate main calculation to DamageManager
                finalDamage = damageManager.calculateFinalPlayerDamage(attacker, victim, finalDamage, isCustomCrit, isBackstab);

                // Apply on-damage-modify enchantment effects
                if (weapon != null && !weapon.getType().isAir()) {
                    Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
                    for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                        CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                        if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                        EnchantmentEffect effect = enchantmentEffectManager.getEffect(enchantment.getCustomLogicKey());
                        if (effect != null) {
                            finalDamage = effect.onDamageModify(finalDamage, event, enchantment, entry.getValue(), isCustomCrit);
                        }
                    }
                }
            } else if (event.getDamager() instanceof LivingEntity mobAttacker) {
                if (mobManager.isCustomMob(mobAttacker)) {
                    finalDamage = mobManager.getTemplate(mobManager.getMobId(mobAttacker)).getStat(Stat.DAMAGE);
                }
            }
        }

        // --- NEW DAMAGE APPLICATION LOGIC ---
        if (victim instanceof Player victimPlayer) {
            // Cancel the vanilla event. We are now in full control of player health.
            event.setCancelled(true);

            // Apply defense calculation
            finalDamage = damageManager.applyDefense(victim, finalDamage);

            // Deal damage to our custom health pool
            playerManager.dealDamage(victimPlayer, finalDamage);

            // Manually play the hurt animation since we cancelled the event
            victimPlayer.playHurtAnimation(0);

            // Show damage indicator
            hudManager.showDamageIndicator(victim.getLocation(), finalDamage, isCustomCrit);

            // Apply on-damaged effects from armor enchantments
            for (ItemStack armorPiece : victimPlayer.getInventory().getArmorContents()) {
                if (armorPiece == null || armorPiece.getType().isAir()) continue;
                Map<String, Integer> enchantments = enchantmentManager.getEnchantments(armorPiece);
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                    if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                    EnchantmentEffect effect = enchantmentEffectManager.getEffect(enchantment.getCustomLogicKey());
                    if (effect != null) {
                        effect.onDamaged(event, enchantment, entry.getValue());
                    }
                }
            }
        } else {
            // For non-player entities (mobs), we apply defense and set the damage on the event.
            finalDamage = damageManager.applyDefense(victim, finalDamage);
            victim.setMetadata(Keys.LAST_HIT_CRIT.getKey(), new FixedMetadataValue(plugin, isCustomCrit));
            event.setDamage(Math.floor(finalDamage));
        }

        // --- This logic runs after damage is finalized for both players and mobs ---
        if (event.getDamager() instanceof Player attacker) {
            // Record hit for combo enchantments
            combatTracker.recordHit(attacker, victim);

            // Apply on-attack effects from weapon enchantments
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (weapon != null && !weapon.getType().isAir()) {
                Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                    if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                    EnchantmentEffect effect = enchantmentEffectManager.getEffect(enchantment.getCustomLogicKey());
                    if (effect != null) {
                        effect.onAttack(event, enchantment, entry.getValue(), isCustomCrit);
                    }
                }
            }
        }
    }

    // --- THIS METHOD IS LARGELY UNCHANGED AS IT DEALS WITH DEATH, NOT DAMAGE ---
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        LivingEntity victim = event.getEntity();
        String mobId = mobManager.getMobId(victim);

        if (mobId != null) {
            // Slayer Boss Kill Logic
            ActiveSlayerQuest quest = slayerManager.getActiveSlayerQuest(killer);
            if (quest != null && quest.getState() == ActiveSlayerQuest.QuestState.BOSS_FIGHT) {
                if (victim.getUniqueId().equals(quest.getActiveBoss().getUniqueId())) {
                    quest.setState(ActiveSlayerQuest.QuestState.AWAITING_CLAIM);
                    quest.setActiveBoss(null);
                    scoreboardManager.updateScoreboard(killer);
                    killer.sendMessage(MMORPGCore.getInstance().getMiniMessage().deserialize("<green><b>SLAYER BOSS SLAIN!</b></green> <gray>Visit Maddox to claim your rewards.</gray>"));
                    killer.playSound(killer.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.2f);

                    PlayerSlayerData slayerData = slayerDataManager.getData(killer);
                    if (slayerData != null) {
                        slayerData.setHighestTierDefeated(quest.getSlayerType(), quest.getTier());
                    }

                    ConfigurationSection bossConfig = slayerManager.getBossConfigById(mobId);
                    if (bossConfig != null) {
                        int xpReward = bossConfig.getInt("slayer-xp-reward", 0);
                        slayerManager.addSlayerExperience(killer, quest.getSlayerType(), xpReward);
                    }
                }
            }

            // Loot Drop Logic
            event.getDrops().clear();
            event.setDroppedExp(0);

            List<ItemStack> finalLoot;
            ConfigurationSection bossConfig = slayerManager.getBossConfigById(mobId);

            if (bossConfig != null) {
                finalLoot = lootManager.rollSlayerLoot(killer, bossConfig);
            } else {
                finalLoot = lootManager.rollLootTable(killer, mobId);
            }

            for (ItemStack drop : finalLoot) {
                String collectionId = collectionManager.getCollectionId(drop.getType());
                if (collectionId != null) {
                    collectionManager.addProgress(killer, collectionId, drop.getAmount());
                }
                org.nakii.mmorpg.util.ItemDropper.dropPristineItem(killer, victim.getLocation(), drop);
            }
        }

        // Grant Skill XP
        skillManager.handleMobKill(killer, victim);

        // Handle On-Kill Enchantment Effects
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir()) return;

        Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
        if (enchantments.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
            if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
            EnchantmentEffect effect = enchantmentEffectManager.getEffect(enchantment.getCustomLogicKey());
            if (effect != null) {
                effect.onKill(event, killer, enchantment, entry.getValue());
            }
        }
    }
}