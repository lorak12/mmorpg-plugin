package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class AlchemyListener implements Listener {
    private final MMORPGCore plugin;

    public AlchemyListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPotionTake(InventoryClickEvent event) {
        // Ensure the event is valid for our purpose
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getType() != InventoryType.BREWING) return;

        // We only care when a player takes an item from a result slot
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        // Check if the taken item is a potion
        Material itemType = item.getType();
        if (itemType == Material.POTION || itemType == Material.SPLASH_POTION || itemType == Material.LINGERING_POTION) {
            Player player = (Player) event.getWhoClicked();

            // Get the configured XP amount from skills.yml
            double xpToGive = plugin.getSkillManager().getSkillConfig()
                    .getDouble("skills.alchemy.xp_sources.DEFAULT", 0.0);

            if (xpToGive > 0) {
                plugin.getSkillManager().addExperience(player, Skill.ALCHEMY, xpToGive);
            }
        }
    }
}