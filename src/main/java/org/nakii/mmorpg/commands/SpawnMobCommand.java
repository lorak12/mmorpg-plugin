package org.nakii.mmorpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnMobCommand implements CommandExecutor, TabCompleter {

    private final MMORPGCore plugin;

    public SpawnMobCommand(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.format("<red>This command can only be used by a player.</red>"));
            return true;
        }

        if (!sender.hasPermission("mmorpg.command.spawnmob")) {
            sender.sendMessage(ChatUtils.format("<red>You do not have permission to use this command.</red>"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatUtils.format("<red>Usage: /spawnmob <mob_id></red>"));
            return true;
        }

        Player player = (Player) sender;
        String mobId = args[0];

        if (plugin.getMobManager().spawnMob(mobId, player.getLocation(), null) == null) {
            player.sendMessage(ChatUtils.format("<red>Mob with ID '" + mobId + "' does not exist.</red>"));
        } else {
            player.sendMessage(ChatUtils.format("<green>Spawned " + mobId + " at your location.</green>"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> mobIds = new ArrayList<>(plugin.getMobManager().getMobRegistry().keySet());
            return StringUtil.copyPartialMatches(args[0], mobIds, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}