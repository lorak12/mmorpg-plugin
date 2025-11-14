package org.nakii.mmorpg.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.WorldManager;
import org.nakii.mmorpg.tasks.ClimateTask;
import org.nakii.mmorpg.world.CustomWorld;
import org.nakii.mmorpg.zone.Zone;

import java.util.Arrays;

public class MmorpgDebugCommand implements CommandExecutor {

    private final MMORPGCore plugin;
    private final ClimateTask climateTask;
    private final WorldManager worldManager;

    public MmorpgDebugCommand(MMORPGCore plugin, ClimateTask climateTask, WorldManager worldManager) {
        this.plugin = plugin;
        this.climateTask = climateTask;
        this.worldManager = worldManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("mmorpg.admin.debug")) {
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
            case "world" -> debugWorld(player);
            case "zone" -> debugZone(player);
            case "climate" -> toggleClimateDebug(player);
            // We can add more later, e.g., "mobcount", "playerstate"
            default -> sendHelpMessage(player);
        }

        return true;
    }

    private void debugWorld(Player player) {
        player.sendMessage(header("World Debug"));

        CustomWorld world = worldManager.getCustomWorld(player.getWorld().getName());
        if (world == null) {
            player.sendMessage(info("World Status", "Not a managed CustomWorld."));
            return;
        }

        player.sendMessage(info("World Name", world.getBukkitWorld().getName()));
        player.sendMessage(info("Display Name", world.getDisplayName()));
        player.sendMessage(info("Total Zones Loaded", String.valueOf(world.getZones().size())));
        player.sendMessage(subheader("World Flags:"));
        player.sendMessage(info("  ├ block-break", String.valueOf(world.getFlags().canBreakBlocks())));
        player.sendMessage(info("  ├ block-place", String.valueOf(world.getFlags().canPlaceBlocks())));
        player.sendMessage(info("  └ pvp", world.getFlags().pvpMode()));
    }

    private void debugZone(Player player) {
        player.sendMessage(header("Zone Debug"));

        Zone zone = worldManager.getZoneForLocation(player.getLocation());
        if (zone == null) {
            player.sendMessage(info("Current Zone", "Wilderness (no zone)"));
            return;
        }

        player.sendMessage(info("Zone ID", zone.getId()));
        player.sendMessage(info("Display Name", zone.getDisplayName()));
        player.sendMessage(subheader("Zone Flags:"));

        boolean hasFlags = false;
        if (zone.getFlags().climate() != null) {
            player.sendMessage(info("  ├ Climate", zone.getFlags().climate().type()));
            hasFlags = true;
        }
        if (zone.getFlags().blockBreakingFlags() != null) {
            player.sendMessage(info("  ├ Block Breaking", "DEFINED (" + zone.getFlags().blockBreakingFlags().definitions().size() + " nodes)"));
            hasFlags = true;
        }
        if (zone.getFlags().mobSpawningFlags() != null) {
            player.sendMessage(info("  ├ Mob Spawning", "DEFINED (Cap: " + zone.getFlags().mobSpawningFlags().spawnCap() + ")"));
            hasFlags = true;
        }

        if (!hasFlags) {
            player.sendMessage(Component.text("  └ No specific flags defined.", NamedTextColor.GRAY));
        }
    }

    private void toggleClimateDebug(Player player) {
        // This assumes your ClimateTask has a 'toggleDebug(player)' method that returns a boolean
        boolean isDebugging = climateTask.toggleDebug(player);
        if (isDebugging) {
            player.sendMessage(Component.text("Climate debug enabled. You will receive climate updates in chat.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Climate debug disabled.", NamedTextColor.RED));
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(header("MMORPG Debug Help"));
        player.sendMessage(info("/mmorpgdebug world", "Shows info about your current world."));
        player.sendMessage(info("/mmorpgdebug zone", "Shows info about the zone you are standing in."));
        player.sendMessage(info("/mmorpgdebug climate", "Toggles debug messages for the climate system."));
    }

    // --- Helper methods for formatting ---
    private Component header(String text) {
        return Component.text("--- [ ", NamedTextColor.DARK_GRAY)
                .append(Component.text(text, NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" ] ---", NamedTextColor.DARK_GRAY));
    }

    private Component subheader(String text) {
        return Component.text(text, NamedTextColor.YELLOW).decoration(TextDecoration.UNDERLINED, true);
    }

    private Component info(String key, String value) {
        return Component.text("» ", NamedTextColor.DARK_GRAY)
                .append(Component.text(key + ": ", NamedTextColor.AQUA))
                .append(Component.text(value, NamedTextColor.WHITE));
    }
    private Component info(String key, Component value) {
        return Component.text("» ", NamedTextColor.DARK_GRAY)
                .append(Component.text(key + ": ", NamedTextColor.AQUA))
                .append(value);
    }
}