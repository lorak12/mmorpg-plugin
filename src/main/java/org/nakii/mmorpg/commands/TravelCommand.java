package org.nakii.mmorpg.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.TravelManager;
import org.nakii.mmorpg.managers.WorldManager;
import org.nakii.mmorpg.world.CustomWorld;
import org.nakii.mmorpg.zone.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TravelCommand implements CommandExecutor, TabCompleter {

    private final TravelManager travelManager;
    private final WorldManager worldManager;

    public TravelCommand(MMORPGCore plugin) {
        this.travelManager = plugin.getTravelManager();
        this.worldManager = plugin.getWorldManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String type = args[0].toLowerCase();

        switch (type) {
            case "world" -> handleWorldTravel(player, args);
            case "zone" -> handleZoneTravel(player, args);
            default -> sendHelpMessage(player);
        }
        return true;
    }

    private void handleWorldTravel(Player player, String[] args) {
        if (!player.hasPermission("mmorpg.travel.world")) {
            player.sendMessage(Component.text("You do not have permission to travel to other worlds.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /travel world <world_name>", NamedTextColor.RED));
            return;
        }
        String worldName = args[1];
        travelManager.travelToWorld(player, worldName);
    }

    private void handleZoneTravel(Player player, String[] args) {
        if (!player.hasPermission("mmorpg.travel.zone")) {
            player.sendMessage(Component.text("You do not have permission to use zone warps.", NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /travel zone <world_name> <zone_id>", NamedTextColor.RED));
            return;
        }
        String worldName = args[1];
        String zoneId = args[2];
        travelManager.travelToZone(player, worldName, zoneId);
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(Component.text("--- Travel Help ---", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/travel world <world_name>", NamedTextColor.AQUA).append(Component.text(" - Travel to a world's spawn.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/travel zone <world_name> <zone_id>", NamedTextColor.AQUA).append(Component.text(" - Warp to a specific zone.", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return new ArrayList<>();

        if (args.length == 1) {
            // Suggest "world" or "zone"
            return List.of("world", "zone").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String type = args[0].toLowerCase();
            if ("world".equals(type) || "zone".equals(type)) {
                // For both /travel world <arg> and /travel zone <arg>, the first argument is a world name.
                return worldManager.getLoadedWorlds().stream()
                        .map(w -> w.getBukkitWorld().getName())
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && "zone".equalsIgnoreCase(args[0])) {
            // This is for the third argument of /travel zone <world> <zone_id>
            CustomWorld world = worldManager.getCustomWorld(args[1]);
            if (world != null) {
                return world.getZones().stream()
                        // Only suggest zones that actually have a warp point defined.
                        .filter(zone -> zone.getWarpPoint().isPresent())
                        .map(Zone::getId)
                        .filter(id -> id.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>(); // Return empty list for no suggestions
    }
}