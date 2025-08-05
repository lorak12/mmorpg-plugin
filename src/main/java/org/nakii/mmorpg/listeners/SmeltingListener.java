package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class SmeltingListener implements Listener {
    private final MMORPGCore plugin;

    public SmeltingListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSmelt(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getType() != InventoryType.FURNACE &&
                event.getInventory().getType() != InventoryType.BLAST_FURNACE &&
                event.getInventory().getType() != InventoryType.SMOKER) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack result = event.getCurrentItem();
        if (result == null) return;

        Player player = (Player) event.getWhoClicked();
        String materialName = result.getType().name();
        double xpToGive = plugin.getSkillManager().getSkillConfig()
                .getDouble("skills.smelting.xp_sources." + materialName, 0.0);

        if (xpToGive > 0) {
            // Multiply by amount taken if shift-clicked
            plugin.getSkillManager().addExperience(player, Skill.SMELTING, xpToGive * result.getAmount());
        }
    }
}