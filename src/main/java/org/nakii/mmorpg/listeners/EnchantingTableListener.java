package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.EnchantingGui;
import org.nakii.mmorpg.managers.EnchantmentManager;
import org.nakii.mmorpg.managers.SkillManager;

public class EnchantingTableListener implements Listener {

    private final MMORPGCore plugin;
    private final EnchantmentManager enchantmentManager;
    private final SkillManager skillManager;

    public EnchantingTableListener(MMORPGCore plugin, EnchantmentManager enchantmentManager, SkillManager skillManager) {
        this.plugin = plugin;
        this.enchantmentManager = enchantmentManager;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.ENCHANTING_TABLE) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        new EnchantingGui(plugin, player, clickedBlock, enchantmentManager, skillManager).open();
    }
}