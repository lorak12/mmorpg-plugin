package org.nakii.mmorpg.listeners;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.requirements.Requirement;
import org.nakii.mmorpg.utils.ChatUtils;

import java.lang.reflect.Type;
import java.util.List;

public class RequirementListener implements Listener {

    private final MMORPGCore plugin;
    private final Gson gson = new Gson();

    public RequirementListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    // Check for weapon requirements when attacking
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (!checkRequirements(player, player.getInventory().getItemInMainHand())) {
                event.setCancelled(true);
            }
        }
    }

    // Check for tool/weapon requirements on left-click and ability requirements on right-click
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!checkRequirements(event.getPlayer(), event.getItem())) {
            event.setCancelled(true);
        }
    }

    // Check for armor requirements when equipping
    @EventHandler
    public void onEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) return;

        if (!checkRequirements(player, event.getCursor())) { // Checking the item on the cursor
            event.setCancelled(true);
        }
    }

    /**
     * The core logic. Checks an item for requirements and validates them against a player.
     * @return True if the player meets all requirements, false otherwise.
     */
    private boolean checkRequirements(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return true;

        var data = item.getItemMeta().getPersistentDataContainer();
        if (!data.has(ItemManager.REQUIREMENTS_KEY, PersistentDataType.STRING)) return true;

        String reqJson = data.get(ItemManager.REQUIREMENTS_KEY, PersistentDataType.STRING);
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> reqStrings = gson.fromJson(reqJson, type);

        for (String reqString : reqStrings) {
            Requirement requirement = Requirement.fromString(reqString);
            if (requirement != null && !requirement.meets(player)) {
                player.sendMessage(ChatUtils.format("<red>You do not meet the requirements to use this item!</red>"));
                // In the future, you can add a more detailed message from the requirement object itself.
                return false;
            }
        }
        return true;
    }
}