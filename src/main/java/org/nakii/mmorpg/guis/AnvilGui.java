package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.EnchantmentManager;
import org.nakii.mmorpg.skills.Skill;

import java.util.List;
import java.util.Map;

public class AnvilGui extends AbstractGui {

    public AnvilGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public String getTitle() { return "<dark_gray><b>Custom Anvil</b></dark_gray>"; }
    @Override
    public int getSize() { return 54; }

    @Override
    public void populateItems() {
        inventory.clear();
        drawBaseLayout(); // This draws the standard border and non-functional pagination

        // --- Layout ---
        // Input slots are cleared for the player
        inventory.setItem(13, null); // Left Input
        inventory.setItem(31, null); // Right Input

        // Static items
        inventory.setItem(22, createItem(Material.ANVIL, "<green>Combine or Repair</green>"));

        // Output slot with placeholder
        inventory.setItem(24, createItem(Material.BARRIER, "<red>Invalid Combination</red>"));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true); // Default to cancelled
        int slot = event.getSlot();

        // Allow interaction with the two input slots
        if (slot == 13 || slot == 31) {
            event.setCancelled(false);
            plugin.getServer().getScheduler().runTask(plugin, this::updateAnvilResult);
            return;
        }

        // Handle clicking the output slot
        if (slot == 24 && event.getClickedInventory() == inventory) {
            ItemStack resultItem = inventory.getItem(24);
            if (resultItem == null || resultItem.getType() == Material.BARRIER) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // TODO: Implement XP cost check from the item's lore

            // Consume input items
            inventory.setItem(13, null);
            inventory.setItem(31, null);

            // Give result and play sound
            player.getInventory().addItem(resultItem);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);

            // Reset the GUI
            updateAnvilResult();
        }

        // Handle standard close button
        if (slot == 49 && event.getClickedInventory() == inventory) {
            player.closeInventory();
        }
    }

    private void updateAnvilResult() {
        ItemStack targetItem = inventory.getItem(13);
        ItemStack sacrificeItem = inventory.getItem(31);

        // If either slot is empty, show barrier and exit
        if (targetItem == null || sacrificeItem == null) {
            inventory.setItem(24, createItem(Material.BARRIER, "<red>Invalid Combination</red>"));
            return;
        }

        EnchantmentManager em = plugin.getEnchantmentManager();
        ItemStack result = null;
        int cost = 0;

        // --- Logic Case 1: Combining two enchanted books ---
        if (targetItem.getType() == Material.ENCHANTED_BOOK && sacrificeItem.getType() == Material.ENCHANTED_BOOK) {
            Map<String, Integer> targetEnchants = em.getEnchantments(targetItem);
            Map<String, Integer> sacrificeEnchants = em.getEnchantments(sacrificeItem);

            // Must be one enchant on each book, and they must be the same enchant and level
            if (targetEnchants.size() == 1 && targetEnchants.equals(sacrificeEnchants)) {
                Map.Entry<String, Integer> entry = targetEnchants.entrySet().iterator().next();
                CustomEnchantment enchant = em.getEnchantment(entry.getKey());
                int currentLevel = entry.getValue();
                int newLevel = currentLevel + 1;

                if (newLevel <= enchant.getMaxLevel()) {
                    result = new ItemStack(Material.ENCHANTED_BOOK);
                    em.addEnchantment(result, enchant.getId(), newLevel);
                    cost = 20 * newLevel; // Example cost
                }
            }
        }
        // --- Logic Case 2: Applying a book to an item ---
        else if (sacrificeItem.getType() == Material.ENCHANTED_BOOK) {
            Map<String, Integer> bookEnchants = em.getEnchantments(sacrificeItem);
            if (bookEnchants.size() == 1) { // Only allow books with one enchant
                Map.Entry<String, Integer> entry = bookEnchants.entrySet().iterator().next();
                String enchantId = entry.getKey();
                int bookLevel = entry.getValue();

                result = applyBookToItem(targetItem.clone(), enchantId, bookLevel);
                cost = 10 * bookLevel; // Example cost
            }
        }

        // --- Display the result ---
        if (result != null) {
            inventory.setItem(24, createItem(result.getType(), "<green>Result</green>", List.of("<gray>Cost: <yellow>" + cost + " Levels</yellow>")));
        } else {
            inventory.setItem(24, createItem(Material.BARRIER, "<red>Invalid Combination</red>"));
        }
    }

    private ItemStack applyBookToItem(ItemStack target, String enchantId, int bookLevel) {
        EnchantmentManager em = plugin.getEnchantmentManager();
        CustomEnchantment enchant = em.getEnchantment(enchantId);

        if (enchant == null || em.getApplicableTypeForItem(target.getType()) == null) return null; // Not applicable

        Map<String, Integer> targetEnchants = em.getEnchantments(target);
        int currentLevel = targetEnchants.getOrDefault(enchantId, 0);

        // Standard anvil logic: new level is book's level if not present, or +1 if levels match
        int newLevel = -1;
        if (currentLevel == 0) {
            newLevel = bookLevel;
        } else if (currentLevel == bookLevel) {
            newLevel = currentLevel + 1;
        }

        if (newLevel != -1 && newLevel <= enchant.getMaxLevel()) {
            em.addEnchantment(target, enchantId, newLevel);
            return target;
        }

        return null; // Invalid combination
    }
}