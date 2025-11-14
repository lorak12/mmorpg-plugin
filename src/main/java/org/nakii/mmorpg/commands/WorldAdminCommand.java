package org.nakii.mmorpg.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.WorldManager;

import java.util.Arrays;
import java.util.List;

public class WorldAdminCommand implements CommandExecutor {

    private final MMORPGCore plugin;
    private final WorldManager worldManager;
    private static final List<String> SUB_COMMANDS = List.of("populate", "teleport", "setflag");

    public WorldAdminCommand(MMORPGCore plugin, WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mmorpg.admin.world")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "populate" -> handlePopulate(sender, subArgs);
            // We can add handlers for teleport, setflag, etc. later
            default -> sendHelpMessage(sender);
        }

        return true;
    }

    private void handlePopulate(CommandSender sender, String[] args) {
        // Syntax: /wa populate <world> <zone> <mask_mat> <primary_node:count|%> [default_node]
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /wa populate <world> <zone> <mask_mat> <primary_node:count|%> [default_node]", NamedTextColor.RED));
            return;
        }

        String worldName = args[0];
        String zoneId = args[1];
        String maskMaterialStr = args[2].toUpperCase();
        String primaryArg = args[3];
        String defaultNodeId = (args.length > 4) ? args[4] : "STONE"; // Default to STONE if not provided

        Material maskMaterial;
        try {
            maskMaterial = Material.valueOf(maskMaterialStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid mask material: " + maskMaterialStr, NamedTextColor.RED));
            return;
        }

        // We will add the core logic to the WorldManager to keep this command class clean.
        worldManager.populateZone(sender, worldName, zoneId, maskMaterial, primaryArg, defaultNodeId);
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("--- World Admin Help ---", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/wa populate <world> <zone> ...", NamedTextColor.AQUA).append(Component.text(" - Populates a zone with nodes.", NamedTextColor.GRAY)));
        // Add other commands here as we implement them.
    }
}