package org.nakii.mmorpg.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LootManager {

    private final MMORPGCore plugin;
    private final Random random = new Random();

    public LootManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Processes a mob's loot table and generates a list of item drops.
     * @param looter The player who killed the mob, used for Magic Find calculations.
     * @param mobId The ID of the custom mob that was killed.
     * @return A List of ItemStacks to be dropped.
     */
    public List<ItemStack> rollLootTable(Player looter, String mobId) {
        List<ItemStack> loot = new ArrayList<>();
        CustomMobTemplate template = plugin.getMobManager().getTemplate(mobId);
        if (template == null || template.getLootTable().isEmpty()) {
            return loot; // No template or loot table found for this mob
        }

        PlayerStats looterStats = plugin.getStatsManager().getStats(looter);
        double magicFind = looterStats.getStat(Stat.MAGIC_FIND);

        for (CustomMobTemplate.LootDrop dropInfo : template.getLootTable()) {
            double chance = dropInfo.chance();

            // Apply Magic Find to the chance
            if (dropInfo.magicFind()) {
                chance *= (1 + (magicFind / 100.0));
            }

            // Roll the dice for the drop
            if (ThreadLocalRandom.current().nextDouble() <= chance) {
                int amount = parseQuantity(dropInfo.quantity());
                if (amount <= 0) continue;

                ItemStack item = plugin.getItemManager().createItemStack(dropInfo.itemId());
                if (item != null) {
                    item.setAmount(amount);
                    // Generate the final lore for the player who is receiving it
                    plugin.getItemLoreGenerator().updateLore(item, looter);
                    loot.add(item);
                }
            }
        }
        return loot;
    }

    /**
     * Parses a quantity string like "1" or "1-3" into a random amount.
     */
    private int parseQuantity(String quantityStr) {
        if (quantityStr.contains("-")) {
            String[] parts = quantityStr.split("-");
            try {
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                if (min >= max) return min; // Handle case like "3-1"
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return 0;
            }
        } else {
            try {
                return Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }



    // --- START OF NEW SLAYER LOOT LOGIC ---

    /**
     * Processes the advanced loot table for a Slayer Boss kill.
     * @param killer The player who killed the boss.
     * @param bossConfig The ConfigurationSection for the specific boss tier from slayers.yml.
     * @return A list of items to be dropped.
     */
    public List<ItemStack> rollSlayerLoot(Player killer, ConfigurationSection bossConfig) {
        List<ItemStack> finalLoot = new ArrayList<>();
        if (bossConfig == null) return finalLoot;

        // 1. Process Guaranteed Drops
        List<String> guaranteedDrops = bossConfig.getStringList("loot.guaranteed");
        for (String dropString : guaranteedDrops) {
            parseAndAddItem(dropString, finalLoot);
        }

        // 2. Process the Rare Drop Pool
        ConfigurationSection rarePoolSection = bossConfig.getConfigurationSection("loot.rare-pool");
        if (rarePoolSection != null) {
            double totalChance = rarePoolSection.getKeys(false).stream()
                    .mapToDouble(rarePoolSection::getDouble)
                    .sum();

            double roll = random.nextDouble() * totalChance;

            double cumulativeChance = 0.0;
            for (String itemKey : rarePoolSection.getKeys(false)) {
                cumulativeChance += rarePoolSection.getDouble(itemKey);
                if (roll < cumulativeChance) {
                    // This is the chosen rare drop
                    parseAndAddItem(itemKey + ":1", finalLoot); // Add quantity of 1
                    break; // Exit after finding one drop
                }
            }
        }

        return finalLoot;
    }

    /**
     * A helper method to parse a drop string (e.g., "ROTTEN_FLESH:10-20") and add it to the loot list.
     * @param dropString The string to parse.
     * @param lootList The list to add the resulting ItemStack to.
     */
    private void parseAndAddItem(String dropString, List<ItemStack> lootList) {
        String[] mainParts = dropString.split(":");
        String itemId = mainParts[0];

        int quantity = 1;
        if (mainParts.length > 1) {
            String quantityPart = mainParts[1];
            if (quantityPart.contains("-")) {
                // Handle a range like "10-20"
                String[] rangeParts = quantityPart.split("-");
                try {
                    int min = Integer.parseInt(rangeParts[0]);
                    int max = Integer.parseInt(rangeParts[1]);
                    if (max > min) {
                        quantity = random.nextInt(max - min + 1) + min;
                    }
                } catch (NumberFormatException ignored) {}
            } else {
                // Handle a single number
                try {
                    quantity = Integer.parseInt(quantityPart);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (quantity > 0) {
            ItemStack item = plugin.getItemManager().createItemStack(itemId);
            if (item != null) {
                item.setAmount(quantity);
                lootList.add(item);
            } else {
                plugin.getLogger().warning("Loot table references unknown item ID: " + itemId);
            }
        }
    }
}