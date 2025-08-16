package org.nakii.mmorpg.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.economy.PlayerEconomy;
import org.nakii.mmorpg.managers.EconomyManager;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EcoCommand implements CommandExecutor, TabCompleter {

    private final EconomyManager economyManager;

    public EcoCommand(MMORPGCore plugin) {
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 4) {
            sendUsage(sender);
            return true;
        }

        String operation = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        String targetAccount = args[2].toLowerCase();
        double amount;

        if (target == null) {
            sender.sendMessage(ChatUtils.format("<red>Player '" + args[1] + "' not found.</red>"));
            return true;
        }

        try {
            amount = Double.parseDouble(args[3]);
            if (amount < 0) {
                sender.sendMessage(ChatUtils.format("<red>Amount cannot be negative.</red>"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtils.format("<red>Invalid amount specified.</red>"));
            return true;
        }

        PlayerEconomy economy = economyManager.getEconomy(target);

        switch (operation) {
            case "set" -> {
                if ("purse".equals(targetAccount)) {
                    economy.setPurse(amount);
                } else if ("bank".equals(targetAccount)) {
                    economy.setBank(amount);
                } else {
                    sendUsage(sender);
                    return true;
                }
                sender.sendMessage(ChatUtils.format("<green>Set " + target.getName() + "'s " + targetAccount + " to " + amount + " coins.</green>"));
            }
            case "add" -> {
                if ("purse".equals(targetAccount)) {
                    economy.addPurse(amount);
                } else if ("bank".equals(targetAccount)) {
                    // We don't have a dedicated addBank, so we just set it
                    economy.setBank(economy.getBank() + amount);
                } else {
                    sendUsage(sender);
                    return true;
                }
                sender.sendMessage(ChatUtils.format("<green>Added " + amount + " coins to " + target.getName() + "'s " + targetAccount + ".</green>"));
            }
            case "remove" -> {
                if ("purse".equals(targetAccount)) {
                    economy.removePurse(amount);
                } else if ("bank".equals(targetAccount)) {
                    economy.setBank(Math.max(0, economy.getBank() - amount));
                } else {
                    sendUsage(sender);
                    return true;
                }
                sender.sendMessage(ChatUtils.format("<green>Removed " + amount + " coins from " + target.getName() + "'s " + targetAccount + ".</green>"));
            }
            default -> sendUsage(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("set", "add", "remove"), completions);
        } else if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), completions);
        } else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Arrays.asList("purse", "bank"), completions);
        }
        Collections.sort(completions);
        return completions;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatUtils.format("<red>Usage: /eco <set|add|remove> <player> <purse|bank> <amount></red>"));
    }
}