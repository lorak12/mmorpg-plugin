package org.nakii.mmorpg.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ZoneWand {

    public static final NamespacedKey WAND_KEY = new NamespacedKey("mmorpgcore", "zone_wand");
    private static final Map<UUID, List<Location>> playerSelections = new HashMap<>();

    public static ItemStack getWand() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.displayName(Component.text("Zone Wand", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Left-Click to add a point.", NamedTextColor.GRAY),
                Component.text("Right-Click to clear points.", NamedTextColor.GRAY),
                Component.text("Use with /zone commands.", NamedTextColor.DARK_GRAY)
        ));
        meta.getPersistentDataContainer().set(WAND_KEY, PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        return wand;
    }

    public static boolean isWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(WAND_KEY, PersistentDataType.BYTE);
    }

    public static void addPoint(Player player, Location location) {
        playerSelections.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(location);
        int pointCount = playerSelections.get(player.getUniqueId()).size();
        player.sendMessage(Component.text("Added point #" + pointCount + " at " + formatLocation(location), NamedTextColor.GREEN));
    }

    public static void clearPoints(Player player) {
        playerSelections.remove(player.getUniqueId());
        player.sendMessage(Component.text("Cleared all selection points.", NamedTextColor.YELLOW));
    }

    public static List<Location> getPoints(Player player) {
        return playerSelections.getOrDefault(player.getUniqueId(), Collections.emptyList());
    }

    private static String formatLocation(Location loc) {
        return String.format("X: %.1f, Z: %.1f", loc.getX(), loc.getZ());
    }
}