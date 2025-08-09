package org.nakii.mmorpg.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.listeners.ZoneWandListener;
import org.nakii.mmorpg.utils.ChatUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MmorpgCommand implements CommandExecutor, TabCompleter {

    private final MMORPGCore plugin;
    private final ZoneWandListener zoneWandListener;

    /**
     * The command now accepts the ZoneWandListener instance directly.
     * This is a clean and reliable way to get access to it.
     */
    public MmorpgCommand(MMORPGCore plugin, ZoneWandListener zoneWandListener) {
        this.plugin = plugin;
        this.zoneWandListener = zoneWandListener;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtils.format("<red>Usage: /mmorpg <subcommand>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "zone":
                handleZoneCommand(sender, args);
                break;
            case "wand":
                handleWandCommand(sender);
                break;
            default:
                sender.sendMessage(ChatUtils.format("<red>Unknown subcommand."));
                break;
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("mmorpg.admin.reload")) {
            sender.sendMessage(ChatUtils.format("<red>You do not have permission."));
            return;
        }
        plugin.getItemManager().loadItems();
        plugin.getMobManager().loadMobs();
        plugin.getZoneManager().loadZones();
        // reload other managers...
        sender.sendMessage(ChatUtils.format("<green>MMORPGCore configs reloaded successfully.</green>"));
    }

    private void handleWandCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.format("<red>This command can only be used by a player."));
            return;
        }
        if (!sender.hasPermission("mmorpg.admin.wand")) {
            sender.sendMessage(ChatUtils.format("<red>You do not have permission."));
            return;
        }

        Player player = (Player) sender;
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.displayName(ChatUtils.format("<gold><b>Zone Wand</b></gold>"));
        meta.lore(ChatUtils.formatList(Arrays.asList(
                "<gray>Left-click to set Position 1.",
                "<gray>Right-click to set Position 2."
        )));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "zone_wand"), PersistentDataType.BOOLEAN, true);
        wand.setItemMeta(meta);

        player.getInventory().addItem(wand);
        player.sendMessage(ChatUtils.format("<green>You have received the Zone Wand!</green>"));
    }

    private void handleZoneCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.format("<red>Zone commands can only be used by a player."));
            return;
        }
        if (!sender.hasPermission("mmorpg.admin.zone")) {
            sender.sendMessage(ChatUtils.format("<red>You do not have permission."));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatUtils.format("<red>Usage: /mmorpg zone create <zone_id>"));
            sender.sendMessage(ChatUtils.format("<red>Usage: /mmorpg zone createsub <parent_id> <subzone_id>"));
            return;
        }

        Player player = (Player) sender;
        String action = args[1].toLowerCase();
        String zoneId = args[2];

        if (action.equals("create")) {
            createZone(player, zoneId, false, null);
        } else if (action.equals("createsub") && args.length == 4) {
            String parentId = args[2];
            String subZoneId = args[3];
            createZone(player, subZoneId, true, parentId);
        } else {
            sender.sendMessage(ChatUtils.format("<red>Invalid zone command usage."));
        }
    }

    private void createZone(Player player, String id, boolean isSubZone, String parentId) {
        if (zoneWandListener == null) {
            player.sendMessage(ChatUtils.format("<red>Error: Could not find ZoneWandListener instance."));
            return;
        }

        Location pos1 = zoneWandListener.getPosition1(player);
        Location pos2 = zoneWandListener.getPosition2(player);

        if (pos1 == null || pos2 == null) {
            player.sendMessage(ChatUtils.format("<red>You must select two positions with the Zone Wand first!"));
            return;
        }
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage(ChatUtils.format("<red>Positions must be in the same world!"));
            return;
        }

        File zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(zonesFile);

        String path;
        if (isSubZone) {
            if (!config.contains("zones." + parentId)) {
                player.sendMessage(ChatUtils.format("<red>Parent zone '" + parentId + "' does not exist!"));
                return;
            }
            path = "zones." + parentId + ".sub-zones." + id;
        } else {
            path = "zones." + id;
        }

        config.set(path + ".world", pos1.getWorld().getName());
        config.set(path + ".pos1", pos1.toVector());
        config.set(path + ".pos2", pos2.toVector());

        try {
            config.save(zonesFile);
            plugin.getZoneManager().loadZones(); // Reload zones to make it active immediately
            player.sendMessage(ChatUtils.format("<green>Successfully created zone '" + id + "'!</green>"));
        } catch (IOException e) {
            player.sendMessage(ChatUtils.format("<red>An error occurred while saving the zone. See console.</red>"));
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "zone", "wand");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("zone")) {
            return Arrays.asList("create", "createsub");
        }
        return Collections.emptyList();
    }


}