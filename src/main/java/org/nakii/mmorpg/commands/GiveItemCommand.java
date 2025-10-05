package org.nakii.mmorpg.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.util.ChatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GiveItemCommand implements CommandExecutor, TabCompleter {

    private final MMORPGCore plugin;

    public GiveItemCommand(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mmorpg.command.giveitem")) {
            sender.sendMessage(ChatUtils.format("<red>You do not have permission to use this command.</red>"));
            return true;
        }


        if (args.length < 1) {
            sender.sendMessage(ChatUtils.format("<red>Usage: /giveitem <item_id> [amount] [player]</red>"));
            return true;
        }

        String itemId = args[0];
        int amount = 1;
        Player target = null;

        // Case 1: /giveitem <item> [amount] [player]
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatUtils.format("<red>Invalid amount specified. Must be a number.</red>"));
                return true;
            }
        }

        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(ChatUtils.format("<red>Player '" + args[2] + "' not found.</red>"));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatUtils.format("<red>You must specify a player when running from console.</red>"));
                return true;
            }
            target = (Player) sender;
        }

        // 1. Create the item with its NBT data
        ItemStack item = plugin.getItemManager().createItemStack(itemId);
        if (item == null) {
            sender.sendMessage(ChatUtils.format("<red>Item ID '" + itemId + "' not found!</red>"));
            return true;
        }
        item.setAmount(amount);

        // 2. Generate and apply the visual lore
        plugin.getItemLoreGenerator().updateLore(item, ((Player) sender).getPlayer());

        // 3. Give the fully formed item to the player
        target.getInventory().addItem(item);

        sender.sendMessage(ChatUtils.format("<green>Gave " + target.getName() + " " + amount + "x " + itemId + ".</green>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // Suggest item names
            List<String> itemNames = new ArrayList<>(plugin.getItemManager().getCustomItems().keySet());
            return StringUtil.copyPartialMatches(args[0], itemNames, new ArrayList<>());
        }
        if (args.length == 3) {
            // Suggest player names
            List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[2], playerNames, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}