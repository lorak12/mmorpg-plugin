package org.nakii.mmorpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.CraftingGui;
import org.nakii.mmorpg.managers.ItemLoreGenerator;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.managers.RecipeManager;

public class CraftCommand implements CommandExecutor {

    private final MMORPGCore plugin;
    private final RecipeManager recipeManager;
    private final ItemManager itemManager;
    private final ItemLoreGenerator itemLoreGenerator;


    public CraftCommand(MMORPGCore plugin, RecipeManager recipeManager, ItemManager itemManager, ItemLoreGenerator itemLoreGenerator) {
        this.plugin = plugin;
        this.recipeManager = recipeManager;
        this.itemManager = itemManager;
        this.itemLoreGenerator = itemLoreGenerator;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }
        new CraftingGui(plugin, player, recipeManager, itemManager, itemLoreGenerator).open();
        return true;
    }
}