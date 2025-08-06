package org.nakii.mmorpg.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.entity.CustomMob;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class LootManager {

    private final MMORPGCore plugin;

    public LootManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Processes the loot table for a given custom mob upon its death.
     * @param mob The custom mob that was killed.
     * @param killer The player who killed the mob, if applicable (for Looting).
     */
    public void processLoot(LivingEntity mob, @Nullable Player killer) {
        String mobId = plugin.getMobManager().getMobId(mob);
        if (mobId == null) return;

        CustomMob customMob = plugin.getMobManager().getCustomMob(mobId);
        if (customMob == null) return;

        ConfigurationSection lootTable = customMob.getConfig().getConfigurationSection("loot_table");
        if (lootTable == null) return;

        int lootingLevel = 0;
        if (killer != null && killer.getEquipment() != null) {
            lootingLevel = killer.getEquipment().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOTING);
        }

        for (String lootKey : lootTable.getKeys(false)) {
            ConfigurationSection lootInfo = lootTable.getConfigurationSection(lootKey);
            if (lootInfo == null){
                // FIX: Add logging
                plugin.getLogger().warning("Mob '" + mobId + "' has no loot_table section in mobs.yml.");
                return;
            }

            double baseDropChance = lootInfo.getDouble("drop_chance", 0.0);
            boolean lootingSensitive = lootInfo.getBoolean("looting_sensitive", false);

            double finalDropChance = baseDropChance;
            // Simple looting formula: adds a percentage of the base chance per level.
            if (lootingSensitive && lootingLevel > 0) {
                finalDropChance += (baseDropChance * 0.5 * lootingLevel); // +50% of base chance per level
            }
            finalDropChance = Math.min(1.0, finalDropChance); // Cap chance at 100%

            // Roll for the drop
            if (ThreadLocalRandom.current().nextDouble() <= finalDropChance) {
                String itemIdentifier = lootInfo.getString("item");
                if (itemIdentifier == null) continue;

                int minAmount = lootInfo.getInt("min_amount", 1);
                int maxAmount = lootInfo.getInt("max_amount", 1);

                // Looting can also increase the max amount of items dropped.
                if (lootingSensitive && lootingLevel > 0) {
                    maxAmount += lootingLevel;
                }

                int amountToDrop = (maxAmount > minAmount) ? ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1) : minAmount;
                if (amountToDrop <= 0) continue;

                ItemStack itemToDrop = getItemStack(itemIdentifier, amountToDrop);
                if (itemToDrop != null) {
                    mob.getWorld().dropItemNaturally(mob.getLocation(), itemToDrop);
                }
            }
        }
    }

    private ItemStack getItemStack(String identifier, int amount) {
        ItemStack item;
        if (identifier.startsWith("custom:")) {
            // Create a custom item using our ItemManager
            item = plugin.getItemManager().createItem(identifier.substring(7), amount);
        } else {
            // Create a vanilla item
            Material material = Material.matchMaterial(identifier.toUpperCase());
            if (material != null) {
                item = new ItemStack(material, amount);
            } else {
                plugin.getLogger().warning("Invalid material in loot table: " + identifier);
                return null;
            }
        }
        return item;
    }
}