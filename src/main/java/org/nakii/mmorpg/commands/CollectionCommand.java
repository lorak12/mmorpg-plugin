package org.nakii.mmorpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.CollectionsGui;
import org.nakii.mmorpg.managers.CollectionManager;

public class CollectionCommand implements CommandExecutor {

    private final MMORPGCore plugin;
    private final CollectionManager collectionManager;

    public CollectionCommand(MMORPGCore plugin, CollectionManager collectionManager) {
        this.plugin = plugin;
        this.collectionManager = collectionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }
        new CollectionsGui(plugin, player, collectionManager).open();
        return true;
    }
}