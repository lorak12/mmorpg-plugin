package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.AnvilGui;

import java.util.Set;

public class AnvilListener implements Listener {

    private final MMORPGCore plugin;
    // Anvils have 3 states, so we check for all of them
    private static final Set<Material> ANVIL_TYPES = Set.of(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL);

    public AnvilListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !ANVIL_TYPES.contains(clickedBlock.getType())) return;

        event.setCancelled(true);
        new AnvilGui(plugin, event.getPlayer()).open();
    }
}