package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.EnchantmentManager;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheHexGui extends AbstractGui {

    // Define key slots for the layout
    private static final int ITEM_SLOT = 13;
    private static final int REAGENT_SLOT = 30;
    private static final int CONFIRM_SLOT = 32;
    private static final int CLOSE_SLOT = 49;

    private CustomEnchantment selectedEnchantment = null;
    private final int REAGENT_COST_AMOUNT = 1;
    private final Material REAGENT_COST_MATERIAL = Material.DIAMOND;

    public TheHexGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public @NotNull String getTitle() {
        return "<dark_purple><b>The Hex</b></dark_purple>";
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public void populateItems() {
        // Draw the static frame
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack purpleFiller = createItem(Material.PURPLE_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < getSize(); i++) {
            inventory.setItem(i, filler);
        }
        for (int i : new int[]{1, 10, 19, 28, 37, 46, 7, 16, 25, 34, 43, 52}) {
            inventory.setItem(i, purpleFiller);
        }

        // Set placeholders and buttons
        inventory.setItem(ITEM_SLOT, null); // Clear input slot
        inventory.setItem(REAGENT_SLOT, null);
        inventory.setItem(CLOSE_SLOT, createItem(Material.BARRIER, "<red><b>Close</b></red>"));

        updateDisplay();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Allow interaction with player inventory and our input slots
        if (clickedInPlayerInventory(event) || event.getSlot() == ITEM_SLOT || event.getSlot() == REAGENT_SLOT) {
            plugin.getServer().getScheduler().runTask(plugin, this::updateDisplay);
            return;
        }

        event.setCancelled(true);
        int slot = event.getSlot();

        if (slot == CLOSE_SLOT) {
            player.closeInventory();
        } else if (slot == CONFIRM_SLOT) {
            handleConfirm();
        } else {
            // Check if the click was on an enchantment icon
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK) {
                // A simple way to store the enchant ID is in the first lore line (hidden)
                String enchantId = getEnchantIdFromLore(clickedItem);
                if (enchantId != null) {
                    this.selectedEnchantment = plugin.getEnchantmentManager().getEnchantment(enchantId);
                    updateDisplay();
                }
            }
        }
    }

    private void handleConfirm() {
        if (selectedEnchantment == null) return;

        ItemStack item = inventory.getItem(ITEM_SLOT);
        ItemStack reagent = inventory.getItem(REAGENT_SLOT);

        // --- Validation ---
        if (item == null) return;
        if (reagent == null || reagent.getType() != REAGENT_COST_MATERIAL || reagent.getAmount() < REAGENT_COST_AMOUNT) {
            player.sendMessage(ChatUtils.format("<red>You need to place " + REAGENT_COST_AMOUNT + " " + REAGENT_COST_MATERIAL.name() + " in the slot.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int cost = getRemovalCost(item, selectedEnchantment);
        if (player.getLevel() < cost) {
            player.sendMessage(ChatUtils.format("<red>You don't have enough XP levels.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // --- Success ---
        player.setLevel(player.getLevel() - cost);
        reagent.setAmount(reagent.getAmount() - REAGENT_COST_AMOUNT);

        // Remove the enchantment and update the item in the slot
        EnchantmentManager em = plugin.getEnchantmentManager();
        em.removeEnchantment(item, selectedEnchantment.getId());

        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 1.5f);

        // Reset selection and refresh the UI
        this.selectedEnchantment = null;
        updateDisplay();
    }

    private void updateDisplay() {
        ItemStack item = inventory.getItem(ITEM_SLOT);
        clearEnchantmentSlots();

        if (item == null) {
            inventory.setItem(22, createItem(Material.PAPER, "<yellow>Place an item above</yellow>", List.of("<gray>Place an item with custom enchantments", "<gray>to begin the removal process.</gray>")));
            inventory.setItem(CONFIRM_SLOT, createItem(Material.GRAY_DYE, "<dark_gray>Select an Enchantment</dark_gray>"));
            return;
        }

        Map<String, Integer> enchants = plugin.getEnchantmentManager().getEnchantments(item);
        if (enchants.isEmpty()) {
            inventory.setItem(22, createItem(Material.BARRIER, "<red>No Custom Enchantments</red>", List.of("<gray>This item has no enchantments to remove.</gray>")));
            inventory.setItem(CONFIRM_SLOT, createItem(Material.GRAY_DYE, "<dark_gray>Select an Enchantment</dark_gray>"));
            return;
        }

        // Display all removable enchantments
        int[] enchantDisplaySlots = {21, 22, 23, 24};
        List<String> enchantIds = new ArrayList<>(enchants.keySet());
        for (int i = 0; i < Math.min(enchantIds.size(), enchantDisplaySlots.length); i++) {
            String enchantId = enchantIds.get(i);
            CustomEnchantment enchant = plugin.getEnchantmentManager().getEnchantment(enchantId);
            int level = enchants.get(enchantId);

            boolean isSelected = enchant.equals(selectedEnchantment);

            List<String> lore = new ArrayList<>();
            // Store ID in lore for retrieval, but make it invisible with a trick
            lore.add("<#010101>" + enchantId);
            lore.add(" ");
            lore.add("<gray>Level: <yellow>" + toRoman(level) + "</yellow></gray>");
            lore.add(" ");
            lore.add(isSelected ? "<green><b>SELECTED</b></green>" : "<yellow>Click to select for removal.</yellow>");

            inventory.setItem(enchantDisplaySlots[i], createItem(Material.ENCHANTED_BOOK, (isSelected ? "<light_purple>" : "<gray>") + enchant.getDisplayName(), lore));
        }

        // Update the confirm button
        if (selectedEnchantment != null) {
            int cost = getRemovalCost(item, selectedEnchantment);
            inventory.setItem(CONFIRM_SLOT, createItem(Material.LIME_DYE, "<green><b>Confirm Removal</b></green>",
                    List.of(
                            "<gray>Removes: " + selectedEnchantment.getDisplayName(),
                            " ",
                            "<gray>Cost: <yellow>" + cost + " XP Levels</yellow>",
                            "<gray>Reagent: <aqua>" + REAGENT_COST_AMOUNT + "x " + REAGENT_COST_MATERIAL.name() + "</aqua>"
                    )
            ));
        } else {
            inventory.setItem(CONFIRM_SLOT, createItem(Material.GRAY_DYE, "<dark_gray>Select an Enchantment</dark_gray>", List.of("<gray>Click an enchantment on the left", "<gray>to see removal costs.</gray>")));
        }
    }

    private int getRemovalCost(ItemStack item, CustomEnchantment enchant) {
        if(item == null || enchant == null) return 999;
        int level = plugin.getEnchantmentManager().getEnchantments(item).getOrDefault(enchant.getId(), 0);
        // Cost is the same as applying it via enchanting table
        return enchant.getCost(level);
    }

    private void clearEnchantmentSlots() {
        for (int slot : new int[]{21, 22, 23, 24}) {
            inventory.setItem(slot, null);
        }
    }

    private String getEnchantIdFromLore(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            String firstLine = item.getItemMeta().getLore().get(0);
            // This is a bit of a hack, assumes MiniMessage format
            if (firstLine.startsWith("§f<#010101>")) {
                return firstLine.substring(10);
            }
        }
        return null;
    }

    // You will need an ItemBuilder utility class and a toRoman method
    private String toRoman(int number) {
        if (number < 1 || number > 39) return String.valueOf(number);
        String[] r = {"X", "IX", "V", "IV", "I"};
        int[] v = {10, 9, 5, 4, 1};
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<v.length; i++) {
            while(number >= v[i]) {
                number -= v[i];
                sb.append(r[i]);
            }
        }
        return sb.toString();
    }

    // You may need to create this helper if it doesn't exist
    private boolean clickedInPlayerInventory(InventoryClickEvent event) {
        return event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Player;
    }
}