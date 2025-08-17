package org.nakii.mmorpg.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.ZoneManager;
import org.nakii.mmorpg.utils.ZoneConfigManager;
import org.nakii.mmorpg.utils.ZoneWand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ZoneCommand implements CommandExecutor, TabCompleter {

    private final ZoneConfigManager configManager;
    private final ZoneManager zoneManager;
    private static final List<String> SUB_COMMANDS = Arrays.asList(
            "wand", "create", "delete", "setpoints", "setheight", "reload"
    );

    public ZoneCommand(MMORPGCore plugin) {
        this.configManager = new ZoneConfigManager(plugin);
        this.zoneManager = plugin.getZoneManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("mmorpg.zone.admin")) {
            player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            case "wand" -> handleWand(player);
            case "create" -> handleCreate(player, subArgs);
            case "delete" -> handleDelete(player, subArgs);
            case "setpoints" -> handleSetPoints(player, subArgs);
            case "setheight" -> handleSetHeight(player, subArgs);
            case "reload" -> handleReload(player);
            default -> sendHelpMessage(player);
        }

        return true;
    }

    private void handleWand(Player player) {
        player.getInventory().addItem(ZoneWand.getWand());
        player.sendMessage(Component.text("You have received the Zone Wand.", NamedTextColor.GREEN));
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /zone create <id> <displayName>", NamedTextColor.RED));
            return;
        }
        String zoneId = args[0].toLowerCase();
        String displayName = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        if (configManager.createZoneFile(zoneId, displayName)) {
            player.sendMessage(Component.text("Successfully created zone file: " + zoneId + ".yml", NamedTextColor.GREEN));
            zoneManager.loadZones();
        } else {
            player.sendMessage(Component.text("A zone with that ID already exists.", NamedTextColor.RED));
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /zone delete <id>", NamedTextColor.RED));
            return;
        }
        String zoneId = args[0].toLowerCase();
        if (configManager.deleteZoneFile(zoneId)) {
            player.sendMessage(Component.text("Successfully deleted zone: " + zoneId, NamedTextColor.GREEN));
            zoneManager.loadZones();
        } else {
            player.sendMessage(Component.text("A zone with that ID does not exist.", NamedTextColor.RED));
        }
    }

    private void handleSetPoints(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /zone setpoints <zoneId> [subZonePath]", NamedTextColor.RED));
            return;
        }
        String zoneId = args[0].toLowerCase();
        String subZonePath = (args.length > 1) ? args[1] : "";

        List<Location> points = ZoneWand.getPoints(player);
        if (points.size() < 3) {
            player.sendMessage(Component.text("You must select at least 3 points to form a polygon.", NamedTextColor.RED));
            return;
        }

        if (configManager.setPoints(zoneId, subZonePath, points)) {
            player.sendMessage(Component.text("Successfully set " + points.size() + " points for " + (subZonePath.isEmpty() ? zoneId : subZonePath) + ".", NamedTextColor.GREEN));
            ZoneWand.clearPoints(player);
            zoneManager.loadZones();
        } else {
            player.sendMessage(Component.text("Failed to set points. Does the zone/sub-zone exist?", NamedTextColor.RED));
        }
    }

    private void handleSetHeight(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /zone setheight <zoneId> <minY> <maxY> [subZonePath]", NamedTextColor.RED));
            return;
        }
        try {
            String zoneId = args[0].toLowerCase();
            int minY = Integer.parseInt(args[1]);
            int maxY = Integer.parseInt(args[2]);
            String subZonePath = (args.length > 3) ? args[3] : "";

            if (configManager.setHeight(zoneId, subZonePath, minY, maxY)) {
                player.sendMessage(Component.text("Successfully set height for " + (subZonePath.isEmpty() ? zoneId : subZonePath) + ".", NamedTextColor.GREEN));
                zoneManager.loadZones();
            } else {
                player.sendMessage(Component.text("Failed to set height. Does the zone/sub-zone exist?", NamedTextColor.RED));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid minY or maxY. They must be numbers.", NamedTextColor.RED));
        }
    }

    private void handleReload(Player player) {
        zoneManager.loadZones();
        player.sendMessage(Component.text("All zones have been reloaded from configuration files.", NamedTextColor.GREEN));
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(Component.text("--- Zone Command Help ---", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone wand", NamedTextColor.AQUA).append(Component.text(" - Gives you the zone selection wand.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/zone create <id> <displayName>", NamedTextColor.AQUA).append(Component.text(" - Creates a new zone file.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/zone delete <id>", NamedTextColor.AQUA).append(Component.text(" - Deletes a zone file.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/zone setpoints <zoneId> [path]", NamedTextColor.AQUA).append(Component.text(" - Saves wand points to a zone.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/zone setheight <zoneId> <min> <max> [path]", NamedTextColor.AQUA).append(Component.text(" - Sets vertical bounds.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/zone reload", NamedTextColor.AQUA).append(Component.text(" - Reloads all zones from files.", NamedTextColor.GRAY)));
    }

    // --- TAB COMPLETION LOGIC ---
    // This is the logic from the deleted ZoneTabCompleter class.
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission("mmorpg.zone.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return SUB_COMMANDS.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "delete":
                case "setpoints":
                case "setheight":
                    return zoneManager.getZoneIds().stream()
                            .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && "setpoints".equalsIgnoreCase(args[0])) {
            String parentZoneId = args[1];
            return zoneManager.getSubZoneIds(parentZoneId).stream()
                    .filter(id -> id.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}