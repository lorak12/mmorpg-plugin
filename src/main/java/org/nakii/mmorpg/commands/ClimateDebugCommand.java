package org.nakii.mmorpg.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.tasks.ClimateTask;

public class ClimateDebugCommand implements CommandExecutor {

    private final ClimateTask climateTask;

    public ClimateDebugCommand(ClimateTask climateTask) {
        this.climateTask = climateTask;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        boolean isDebugging = climateTask.toggleDebug(player);

        if (isDebugging) {
            player.sendMessage(Component.text("Climate debug enabled. View your action bar.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Climate debug disabled.", NamedTextColor.YELLOW));
            // Clear the action bar one last time
            player.sendActionBar(Component.text(""));
        }

        return true;
    }
}