package org.nakii.mmorpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.SlayerGui;
import org.nakii.mmorpg.managers.EconomyManager;
import org.nakii.mmorpg.managers.RequirementManager;
import org.nakii.mmorpg.managers.SlayerDataManager;
import org.nakii.mmorpg.managers.SlayerManager;

public class SlayerCommand implements CommandExecutor {

    private final MMORPGCore plugin;
    private final SlayerManager slayerManager;
    private final SlayerDataManager slayerDataManager;
    private final EconomyManager economyManager;
    private final RequirementManager requirementManager;


    public SlayerCommand(MMORPGCore plugin, SlayerManager slayerManager, SlayerDataManager slayerDataManager, EconomyManager economyManager, RequirementManager requirementManager) {
        this.plugin = plugin;
        this.slayerManager = slayerManager;
        this.slayerDataManager = slayerDataManager;
        this.economyManager = economyManager;
        this.requirementManager = requirementManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        // Open the new Slayer GUI
        new SlayerGui(plugin, player, slayerManager, slayerDataManager, economyManager, requirementManager).open();
        return true;
    }
}