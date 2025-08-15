package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LootManager {

    private final MMORPGCore plugin;

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
}