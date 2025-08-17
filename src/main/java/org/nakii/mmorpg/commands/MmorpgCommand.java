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
import org.nakii.mmorpg.utils.ChatUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MmorpgCommand implements CommandExecutor, TabCompleter {

    private final MMORPGCore plugin;

    /**
     * The command now accepts the ZoneWandListener instance directly.
     * This is a clean and reliable way to get access to it.
     */
    public MmorpgCommand(MMORPGCore plugin) {
        this.plugin = plugin;
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
        plugin.getRecipeManager().loadRecipes();
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