package org.nakii.mmorpg.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.EnchantmentManager;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantCommand implements CommandExecutor, TabCompleter {

    private final MMORPGCore plugin;
    private final EnchantmentManager enchantmentManager;

    public EnchantCommand(MMORPGCore plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatUtils.format("<red>Usage: /customenchant <player> <enchant_id> <level> [material]</red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatUtils.format("<red>Player '" + args[0] + "' not found.</red>"));
            return true;
        }

        CustomEnchantment enchantment = enchantmentManager.getEnchantment(args[1]);
        if (enchantment == null) {
            sender.sendMessage(ChatUtils.format("<red>Enchantment '" + args[1] + "' not found.</red>"));
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[2]);
            if (level <= 0 || level > enchantment.getMaxLevel()) {
                sender.sendMessage(ChatUtils.format("<red>Invalid level. Must be between 1 and " + enchantment.getMaxLevel() + " for this enchantment.</red>"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtils.format("<red>Invalid level specified. Must be a number.</red>"));
            return true;
        }

        ItemStack item;
        // Check if a material was provided
        if (args.length > 3) {
            try {
                Material material = Material.valueOf(args[3].toUpperCase());
                item = new ItemStack(material);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatUtils.format("<red>Invalid material '" + args[3] + "'.</red>"));
                return true;
            }
        } else {
            // Default to an enchanted book
            item = new ItemStack(Material.ENCHANTED_BOOK);
        }

        enchantmentManager.addEnchantment(item, enchantment.getId(), level);

        // Give the item to the player
        if (target.getInventory().addItem(item).isEmpty()) {
            target.sendMessage(ChatUtils.format("<green>You have received an enchanted item!</green>"));
        } else {
            target.getWorld().dropItemNaturally(target.getLocation(), item);
            target.sendMessage(ChatUtils.format("<yellow>Your inventory was full, so the enchanted item was dropped at your feet.</yellow>"));
        }

        sender.sendMessage(ChatUtils.format("<green>Successfully gave " + target.getName() + " an item with " + enchantment.getDisplayName() + " " + toRoman(level) + ".</green>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            // Suggest online player names
            List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            StringUtil.copyPartialMatches(args[0], playerNames, completions);
        } else if (args.length == 2) {
            // Suggest enchantment IDs
            StringUtil.copyPartialMatches(args[1], enchantmentManager.getAllEnchantments().keySet(), completions);
        } else if (args.length == 3) {
            // Suggest levels for the specified enchantment
            CustomEnchantment enchantment = enchantmentManager.getEnchantment(args[1]);
            if (enchantment != null) {
                for (int i = 1; i <= enchantment.getMaxLevel(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
        } else if (args.length == 4) {
            // Suggest materials
            List<String> materialNames = new ArrayList<>();
            for (Material mat : Material.values()) {
                if (mat.isItem() && !mat.isLegacy()) { // Only suggest valid items
                    materialNames.add(mat.name());
                }
            }
            StringUtil.copyPartialMatches(args[3], materialNames, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    private String toRoman(int number) {
        if (number < 1 || number > 39) return String.valueOf(number);
        String[] r = {"X", "IX", "V", "IV", "I"}; int[] v = {10, 9, 5, 4, 1};
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<v.length; i++) { while(number >= v[i]) { number -= v[i]; sb.append(r[i]); } }
        return sb.toString();
    }
}