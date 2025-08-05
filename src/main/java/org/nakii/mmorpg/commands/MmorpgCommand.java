package org.nakii.mmorpg.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MmorpgCommand implements CommandExecutor, TabCompleter {

    private final MMORPGCore plugin;

    public MmorpgCommand(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /mmorpg <give|reload|spawn>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                handleGive(sender, args);
                break;
            case "reload":
                // Check for permission
                if (!sender.hasPermission("mmorpg.command.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                plugin.getSkillManager().loadSkillConfig();
                plugin.getItemManager().loadItems();
                plugin.getRecipeManager().loadRecipes();
                sender.sendMessage(ChatColor.GREEN + "MMORPGCore configuration and items have been reloaded.");
                break;
            case "spawn":
                // Handle spawn logic (as a placeholder)
                sender.sendMessage(ChatColor.YELLOW + "Mob spawning is not yet fully implemented.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /mmorpg <give|reload|spawn>");
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        // /mmorpg give <item_name> [player] [amount]
        if (args.length < 2) {
            sender.sendMessage("Usage: /mmorpg give <item_name> [player] [amount]");
            return;
        }

        String itemName = args[1];
        Player target = (args.length > 2) ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
        int amount = (args.length > 3) ? Integer.parseInt(args[3]) : 1;

        if (target == null) {
            sender.sendMessage("Player not found or not specified.");
            return;
        }

        plugin.getItemManager().giveItem(target, itemName, amount);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "reload", "spawn");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return plugin.getItemManager().getCustomItems().keySet().stream().collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return null;
    }
}