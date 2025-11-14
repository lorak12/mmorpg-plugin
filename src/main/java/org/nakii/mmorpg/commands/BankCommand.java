package org.nakii.mmorpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.BankGui;
import org.nakii.mmorpg.managers.BankManager;
import org.nakii.mmorpg.managers.EconomyManager;
import org.nakii.mmorpg.managers.WorldTimeManager;

public class BankCommand implements CommandExecutor {

    private final MMORPGCore plugin;
    private final WorldTimeManager worldTimeManager;
    private final EconomyManager economyManager;
    private final BankManager bankManager;

    public BankCommand(MMORPGCore plugin, WorldTimeManager worldTimeManager, EconomyManager economyManager, BankManager bankManager) {
        this.plugin = plugin;
        this.worldTimeManager = worldTimeManager;
        this.economyManager = economyManager;
        this.bankManager = bankManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        new BankGui(plugin, player,worldTimeManager, economyManager, bankManager).open();
        return true;
    }
}