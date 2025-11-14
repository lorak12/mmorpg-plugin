package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.util.ChatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A centralized manager for parsing and granting rewards to players.
 */
public class RewardManager {

    private final MMORPGCore plugin;
    private final SkillManager skillManager;
    private final ItemManager itemManager;
    private final StatsManager statsManager;
    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RewardManager(MMORPGCore plugin, SkillManager skillManager, ItemManager itemManager, StatsManager statsManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.itemManager = itemManager;
        this.statsManager = statsManager;
        this.economyManager = economyManager;
    }

    /**
     * Grants a list of rewards to a player and returns a formatted list of what they received.
     *
     * @param player        The player to receive the rewards.
     * @param rewardStrings The list of raw reward strings from a config file.
     * @return A list of formatted Components describing the granted rewards.
     */
    public List<Component> grantRewards(Player player, List<String> rewardStrings) {
        List<Component> grantedRewards = new ArrayList<>();
        if (rewardStrings == null) return grantedRewards;

        for (String rewardString : rewardStrings) {
            String[] parts = rewardString.split(":");
            if (parts.length < 2) continue;

            String type = parts[0].toUpperCase();
            String context = parts[1];

            switch (type) {
                case "SKILL_XP":
                    if (parts.length >= 3) {
                        try {
                            Skill skill = Skill.valueOf(parts[1].toUpperCase());
                            int amount = Integer.parseInt(parts[2]);
                            skillManager.addXp(player, skill, amount);
                            // Format the message for the chat
                            grantedRewards.add(miniMessage.deserialize("<gray>+ <aqua>" + String.format("%,d", amount) + " " + skill.name() + " XP</aqua>"));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid reward string: " + rewardString);
                        }
                    }
                    break;

                case "RECIPE_UNLOCK":
                    // This is now just a notification, as the requirement system handles the actual unlocking.
                    String recipeName = parts[1].replace("_", " ").toLowerCase();
                    grantedRewards.add(miniMessage.deserialize("<gray>+ <green>New Recipe: </green><white>" + ChatUtils.capitalizeWords(recipeName) + "</white>"));
                    break;

                // Future reward types like STAT_BOOST or ITEM_GIVE would be handled here

                case "STAT_BOOST":
                    if (parts.length >= 3) {
                        try {
                            Stat stat = Stat.valueOf(context.toUpperCase());
                            double amount = Double.parseDouble(parts[2]);
                            statsManager.addPermanentBonus(player, stat, amount);
                            // You would need a method in your Stat enum to get its symbol
                            grantedRewards.add(miniMessage.deserialize("<gray>+ <red>" + stat.getSymbol() + amount + " " + stat.getDisplayName() + "</red>"));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid stat in reward string: " + rewardString);
                        }
                    }
                    break;

                case "ITEM_GIVE":
                    if (parts.length >= 3) {
                        int amount = Integer.parseInt(parts[2]);
                        ItemStack item = itemManager.createItemStack(context);
                        if (item == null) {
                            item = itemManager.createVanillaItemStack(Material.matchMaterial(context));
                        }

                        if (item != null) {
                            item.setAmount(amount);
                            grantedRewards.add(miniMessage.deserialize("<gray>+ " + item.displayName().examinableName() + " <gray>x" + amount));

                            // Add to inventory and drop leftovers on the ground
                            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                            if (!leftovers.isEmpty()) {
                                leftovers.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
                            }
                        }
                    }
                    break;

                case "COINS":
                    try {
                        int amount = Integer.parseInt(context);
                        economyManager.getEconomy(player).addPurse(amount);
                        grantedRewards.add(miniMessage.deserialize("<gray>+ <gold>" + String.format("%,d", amount) + " Coins</gold>"));
                    } catch (NumberFormatException ignored) {}
                    break;
            }
        }
        return grantedRewards;
    }
}