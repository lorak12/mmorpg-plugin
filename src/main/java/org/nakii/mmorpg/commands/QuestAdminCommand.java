package org.nakii.mmorpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class QuestAdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Implementation of /questadmin commands:
        // /qa debug player <name>
        // /qa force event <event_string> <player>
        // /qa force objective <start|delete> <obj_id> <player>
        // /qa reload
        return true;
    }
}