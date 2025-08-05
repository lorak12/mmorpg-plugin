package org.nakii.mmorpg.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.nakii.mmorpg.MMORPGCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.entity.CustomZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MmorpgCommand implements CommandExecutor, TabCompleter {

    private final MMORPGCore plugin;
    private static final List<String> BASE_COMMANDS = Arrays.asList("give", "reload", "spawn", "zone");
    private static final List<String> ZONE_COMMANDS = Arrays.asList("wand", "create", "delete", "list", "modify");
    private static final List<String> ZONE_MODIFY_COMMANDS = Arrays.asList("addmob", "removemob");


    public MmorpgCommand(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mmorpg.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /mmorpg <" + String.join("|", BASE_COMMANDS) + ">");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                handleGive(sender, args);
                break;
            case "reload":
                plugin.getSkillManager().loadSkillConfig();
                plugin.getItemManager().loadItems();
                plugin.getMobManager().loadMobs();
                plugin.getZoneManager().loadZones();
                sender.sendMessage(ChatColor.GREEN + "MMORPGCore configurations, items, mobs, and zones have been reloaded.");
                break;
            case "spawn":
                handleSpawn(sender, args);
                break;
            case "zone":
                handleZoneCommand(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /mmorpg <" + String.join("|", BASE_COMMANDS) + ">");
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        // /mmorpg give <item_name> [player] [amount]
        if (args.length < 2) {
            sender.sendMessage("Usage: /mmorpg give <item_name> [player] [amount]");
            return;
        }

        String itemName = args[1];
        Player target = (args.length > 2) ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
        int amount = (args.length > 3) ? Integer.parseInt(args[3]) : 1;

        if (target == null) {
            sender.sendMessage("Player not found or not specified.");
            return;
        }

        plugin.getItemManager().giveItem(target, itemName, amount);
    }

    private void handleZoneCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Zone commands must be run by a player.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /mmorpg zone <" + String.join("|", ZONE_COMMANDS) + ">");
            return;
        }
        Player player = (Player) sender;
        String action = args[1].toLowerCase();

        switch (action) {
            case "wand":
                handleZoneWand(player);
                break;
            case "create":
                handleZoneCreate(player, args);
                break;
            case "delete":
                handleZoneDelete(player, args);
                break;
            case "list":
                handleZoneList(player);
                break;
            case "modify":
                handleZoneModify(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown zone command. Usage: /mmorpg zone <" + String.join("|", ZONE_COMMANDS) + ">");
                break;
        }
    }

    private void handleZoneWand(Player player) {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Zone Wand");
        meta.setLore(Collections.singletonList(ChatColor.YELLOW + "Left/Right click blocks to set positions."));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "zone_wand"), PersistentDataType.INTEGER, 1);
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "You have received the Zone Wand!");
    }

    private void handleZoneCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /mmorpg zone create <zone_id>");
            return;
        }
        String zoneId = args[2].toLowerCase();
        Location pos1 = plugin.getZoneManager().getPlayerPos1(player.getUniqueId());
        Location pos2 = plugin.getZoneManager().getPlayerPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            player.sendMessage(ChatColor.RED + "You must set both positions with the wand first!");
            return;
        }

        FileConfiguration zonesConfig = YamlConfiguration.loadConfiguration(plugin.getZoneManager().getZonesFile());
        String path = "zones." + zoneId;
        zonesConfig.set(path + ".world", player.getWorld().getName());
        zonesConfig.set(path + ".pos1.x", pos1.getX());
        zonesConfig.set(path + ".pos1.y", pos1.getY());
        zonesConfig.set(path + ".pos1.z", pos1.getZ());
        zonesConfig.set(path + ".pos2.x", pos2.getX());
        zonesConfig.set(path + ".pos2.y", pos2.getY());
        zonesConfig.set(path + ".pos2.z", pos2.getZ());

        try {
            zonesConfig.save(plugin.getZoneManager().getZonesFile());
            plugin.getZoneManager().loadZones();
            player.sendMessage(ChatColor.GREEN + "Zone '" + zoneId + "' created successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while saving the zone to file.");
        }
    }

    private void handleZoneDelete(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /mmorpg zone delete <zone_id>");
            return;
        }
        String zoneId = args[2].toLowerCase();
        FileConfiguration zonesConfig = YamlConfiguration.loadConfiguration(plugin.getZoneManager().getZonesFile());

        if (!zonesConfig.contains("zones." + zoneId)) {
            player.sendMessage(ChatColor.RED + "Zone '" + zoneId + "' does not exist.");
            return;
        }

        zonesConfig.set("zones." + zoneId, null); // Remove the entire section
        try {
            zonesConfig.save(plugin.getZoneManager().getZonesFile());
            plugin.getZoneManager().loadZones();
            player.sendMessage(ChatColor.GREEN + "Zone '" + zoneId + "' has been deleted.");
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while saving the zones file.");
        }
    }

    private void handleZoneList(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Custom Mob Zones ---");
        plugin.getZoneManager().getZoneRegistry().keySet().forEach(zoneId -> {
            player.sendMessage(ChatColor.YELLOW + "- " + zoneId);
        });
    }

    private void handleZoneModify(Player player, String[] args) {
        if (args.length < 6 && !"removemob".equalsIgnoreCase(args[3])) {
            player.sendMessage(ChatColor.RED + "Usage: /mmorpg zone modify <zone_id> addmob <mob_id> <chance> <max_count>");
            player.sendMessage(ChatColor.RED + "Usage: /mmorpg zone modify <zone_id> removemob <mob_id>");
            return;
        }

        String zoneId = args[2].toLowerCase();
        String action = args[3].toLowerCase();
        String mobId = args[4].toLowerCase();

        FileConfiguration zonesConfig = YamlConfiguration.loadConfiguration(plugin.getZoneManager().getZonesFile());
        String zonePath = "zones." + zoneId;

        if (!zonesConfig.contains(zonePath)) {
            player.sendMessage(ChatColor.RED + "Zone '" + zoneId + "' does not exist.");
            return;
        }

        if ("addmob".equals(action)) {
            double chance;
            int maxCount;
            try {
                chance = Double.parseDouble(args[5]);
                maxCount = Integer.parseInt(args[6]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid chance or max count. Must be numbers.");
                return;
            }

            String mobPath = zonePath + ".mobs." + mobId;
            zonesConfig.set(mobPath + ".id", mobId);
            zonesConfig.set(mobPath + ".spawn_chance", chance);
            zonesConfig.set(mobPath + ".max_in_zone", maxCount);

            player.sendMessage(ChatColor.GREEN + "Added mob '" + mobId + "' to zone '" + zoneId + "'.");
        } else if ("removemob".equals(action)) {
            String mobPath = zonePath + ".mobs." + mobId;
            if (!zonesConfig.contains(mobPath)) {
                player.sendMessage(ChatColor.RED + "Mob '" + mobId + "' is not in zone '" + zoneId + "'.");
                return;
            }
            zonesConfig.set(mobPath, null);
            player.sendMessage(ChatColor.GREEN + "Removed mob '" + mobId + "' from zone '" + zoneId + "'.");
        } else {
            player.sendMessage(ChatColor.RED + "Unknown modify action. Use 'addmob' or 'removemob'.");
            return;
        }

        try {
            zonesConfig.save(plugin.getZoneManager().getZonesFile());
            plugin.getZoneManager().loadZones();
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while saving the zones file.");
        }
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        // /mmorpg spawn <mob_id> [amount]
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /mmorpg spawn <mob_id> [amount]");
            return;
        }

        Player player = (Player) sender;
        String mobId = args[1];
        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
                return;
            }
        }

        for (int i = 0; i < amount; i++) {
            plugin.getMobManager().spawnMob(mobId, player.getLocation());
        }
        sender.sendMessage(ChatColor.GREEN + "Spawned " + amount + " of " + mobId + ".");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // /mmorpg [?]
            StringUtil.copyPartialMatches(args[0], BASE_COMMANDS, completions);
        } else if (args.length == 2) {
            // /mmorpg <subcommand> [?]
            if (args[0].equalsIgnoreCase("give")) {
                StringUtil.copyPartialMatches(args[1], plugin.getItemManager().getCustomItems().keySet(), completions);
            } else if (args[0].equalsIgnoreCase("spawn")) {
                StringUtil.copyPartialMatches(args[1], plugin.getMobManager().getMobRegistry().keySet(), completions);
            } else if (args[0].equalsIgnoreCase("zone")) {
                StringUtil.copyPartialMatches(args[1], ZONE_COMMANDS, completions);
            }
        } else if (args.length == 3) {
            // /mmorpg <subcommand> <arg2> [?]
            if (args[0].equalsIgnoreCase("give")) {
                StringUtil.copyPartialMatches(args[2], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), completions);
            } else if (args[0].equalsIgnoreCase("zone")) {
                if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("modify")) {
                    StringUtil.copyPartialMatches(args[2], plugin.getZoneManager().getZoneRegistry().keySet(), completions);
                }
            }
        } else if (args.length == 4) {
            // /mmorpg zone modify <zone_id> [?]
            if (args[0].equalsIgnoreCase("zone") && args[1].equalsIgnoreCase("modify")) {
                StringUtil.copyPartialMatches(args[3], ZONE_MODIFY_COMMANDS, completions);
            }
        } else if (args.length == 5) {
            // /mmorpg zone modify <zone_id> <addmob|removemob> [?]
            if (args[0].equalsIgnoreCase("zone") && args[1].equalsIgnoreCase("modify")) {
                if (args[3].equalsIgnoreCase("addmob")) {
                    // Suggest all custom mobs that can be added
                    StringUtil.copyPartialMatches(args[4], plugin.getMobManager().getMobRegistry().keySet(), completions);
                } else if (args[3].equalsIgnoreCase("removemob")) {
                    // Suggest only mobs that are currently in that zone
                    CustomZone zone = plugin.getZoneManager().getZoneRegistry().get(args[2].toLowerCase());
                    if (zone != null) {
                        ConfigurationSection mobsSection = zone.getConfig().getConfigurationSection("mobs");
                        if (mobsSection != null) {
                            StringUtil.copyPartialMatches(args[4], mobsSection.getKeys(false), completions);
                        }
                    }
                }
            }
        }

        Collections.sort(completions);
        return completions;
    }
}