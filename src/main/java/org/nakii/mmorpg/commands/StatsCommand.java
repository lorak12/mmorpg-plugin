package org.nakii.mmorpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;

public class StatsCommand implements CommandExecutor {
    private final MMORPGCore plugin;

    public StatsCommand(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        plugin.getGuiManager().openStatsGUI(player);
        return true;
    }
}