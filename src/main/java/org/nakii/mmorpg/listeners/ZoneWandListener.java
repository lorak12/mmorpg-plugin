package org.nakii.mmorpg.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;

public class ZoneWandListener implements Listener {
    private final MMORPGCore plugin;

    public ZoneWandListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "zone_wand"), PersistentDataType.INTEGER)) return;

        event.setCancelled(true);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location loc = event.getClickedBlock().getLocation();
            plugin.getZoneManager().setPlayerSelection(player.getUniqueId(), 1, loc);
            player.sendMessage(ChatColor.GREEN + "Position 1 set to (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location loc = event.getClickedBlock().getLocation();
            plugin.getZoneManager().setPlayerSelection(player.getUniqueId(), 2, loc);
            player.sendMessage(ChatColor.GREEN + "Position 2 set to (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
        }
    }
}