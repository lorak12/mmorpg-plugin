package org.nakii.mmorpg.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.EnchantmentManager;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantingGui extends AbstractGui {

    // --- STATE MANAGEMENT ---
    private enum ViewState {
        SELECTING_ENCHANTMENT,
        SELECTING_LEVEL
    }
    private ViewState currentState = ViewState.SELECTING_ENCHANTMENT;
    private CustomEnchantment selectedEnchantment = null;
    private ItemStack itemToEnchant; // Field to preserve the item between state changes

    private List<CustomEnchantment> applicableEnchants = new ArrayList<>();
    private static final int ITEM_SLOT = 19;

    public EnchantingGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
        this.maxItemsPerPage = 15;
    }

    // Constructor to carry over the item during state changes
    public EnchantingGui(MMORPGCore plugin, Player player, ItemStack itemToEnchant) {
        this(plugin, player);
        this.itemToEnchant = itemToEnchant;
    }

    @Override
    public String getTitle() {
        if (currentState == ViewState.SELECTING_LEVEL && selectedEnchantment != null) {
            return "<dark_purple>Level: " + selectedEnchantment.getDisplayName() + "</dark_purple>";
        }
        return "<dark_purple><b>Custom Enchanting</b></dark_purple>";
    }

    @Override
    public int getSize() { return 54; }

    @Override
    public void populateItems() {
        // 1. Get the item from our class field IF it exists.
        //    Otherwise, get it from the inventory slot.
        ItemStack currentItem = (this.itemToEnchant != null) ? this.itemToEnchant : inventory.getItem(ITEM_SLOT);
        this.itemToEnchant = null; // Clear the temporary field after using it.

        inventory.clear(); // Clear for redraw

        // Fill entire GUI with filler, then place items
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) { inventory.setItem(i, filler); }

        inventory.setItem(ITEM_SLOT, currentItem); // Put the preserved item back
        inventory.setItem(28, createItem(Material.ENCHANTING_TABLE, "<light_purple>Place Item Above</light_purple>"));
        inventory.setItem(48, createItem(Material.BOOKSHELF, "<gold>Bookshelf Power: 0</gold>"));
        inventory.setItem(49, createItem(Material.BARRIER, "<red><b>Close</b></red>"));
        inventory.setItem(50, createItem(Material.BOOK, "<green>Enchantment Guide</green>"));

        if (currentState == ViewState.SELECTING_ENCHANTMENT) {
            drawEnchantmentSelection(currentItem);
        } else if (currentState == ViewState.SELECTING_LEVEL) {
            drawLevelSelection();
        }
    }

    private void drawEnchantmentSelection(ItemStack itemToEnchant) {
        if (itemToEnchant == null || itemToEnchant.getType() == Material.AIR) {
            applicableEnchants.clear();
            totalPages = 1; page = 0;
            inventory.setItem(22, createItem(Material.GRAY_DYE, "<gray>Applicable Enchantments", List.of("Place an item to see available options.")));
        } else {
            applicableEnchants = plugin.getEnchantmentManager().getApplicableEnchantments(itemToEnchant);
            totalPages = (int) Math.ceil((double) applicableEnchants.size() / maxItemsPerPage);

            int[] enchantSlots = {12, 13, 14, 15, 16, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34};
            int startIndex = page * maxItemsPerPage;

            for (int i = 0; i < maxItemsPerPage; i++) {
                int enchantIndex = startIndex + i;
                if (enchantIndex < applicableEnchants.size()) {
                    CustomEnchantment enchant = applicableEnchants.get(enchantIndex);
                    inventory.setItem(enchantSlots[i], createItem(Material.ENCHANTED_BOOK, "<green>" + enchant.getDisplayName(), enchant.getDescription()));
                } else {
                    inventory.setItem(enchantSlots[i], null); // Clear unused slots
                }
            }
        }

        if (page > 0) inventory.setItem(47, createItem(Material.PLAYER_HEAD, "<green>Previous Page</green>"));
        if (page < totalPages - 1) inventory.setItem(35, createItem(Material.PLAYER_HEAD, "<green>Next Page</green>"));
    }

    private void drawLevelSelection() {
        inventory.setItem(47, createItem(Material.ARROW, "<green>Go Back</green>"));

        int[] levelSlots = {21, 22, 23, 24, 25};
        for (int i = 1; i <= Math.min(selectedEnchantment.getMaxLevel(), 5); i++) {
            int level = i;
            int xpCost = 10 * level;
            inventory.setItem(levelSlots[i-1], createItem(Material.EXPERIENCE_BOTTLE,
                    "<green>" + selectedEnchantment.getDisplayName() + " " + toRoman(level) + "</green>",
                    List.of("<gray>Cost: <yellow>" + xpCost + " Levels</yellow>")));
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getClickedInventory().getHolder() instanceof EnchantingGui)) {
            if (event.isShiftClick()) {
                plugin.getServer().getScheduler().runTask(plugin, this::populateItems);
            }
            return;
        }

        event.setCancelled(true);
        int slot = event.getSlot();

        if (slot == ITEM_SLOT) {
            event.setCancelled(false);
            plugin.getServer().getScheduler().runTask(plugin, this::populateItems);
            return;
        }

        if (currentState == ViewState.SELECTING_ENCHANTMENT) {
            handleEnchantSelectionClick(slot);
        } else if (currentState == ViewState.SELECTING_LEVEL) {
            handleLevelSelectionClick(slot);
        }
    }

    private void handleEnchantSelectionClick(int slot) {
        if (slot == 49) { player.closeInventory(); return; }
        if (slot == 50) { new EnchantmentGuideGui(plugin, player).open(); return; }
        if (slot == 47) { previousPage(); return; }
        if (slot == 35) { nextPage(); return; }

        int[] enchantSlots = {12, 13, 14, 15, 16, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34};
        for (int i = 0; i < enchantSlots.length; i++) {
            if (slot == enchantSlots[i]) {
                int enchantIndex = (page * maxItemsPerPage) + i;
                if (enchantIndex < applicableEnchants.size()) {
                    this.selectedEnchantment = applicableEnchants.get(enchantIndex);
                    this.currentState = ViewState.SELECTING_LEVEL;
                    openAsStateChange();
                }
                return;
            }
        }
    }

    private void handleLevelSelectionClick(int slot) {
        if (slot == 47) { // Back button
            this.currentState = ViewState.SELECTING_ENCHANTMENT;
            this.selectedEnchantment = null;
            openAsStateChange();
            return;
        }
        if (slot == 49) { player.closeInventory(); return; }
        if (slot == 50) { new EnchantmentGuideGui(plugin, player).open(); return; }

        int[] levelSlots = {21, 22, 23, 24, 25};
        for (int i = 0; i < levelSlots.length; i++) {
            if (slot == levelSlots[i]) {
                int level = i + 1;
                int xpCost = 10 * level;
                applyEnchantment(level, xpCost);

                return;
            }
        }
    }

    private void openAsStateChange() {
        // 1. Create a NEW instance of our own GUI.
        EnchantingGui newGui = new EnchantingGui(plugin, player);

        // 2. Carry over the state to the new instance.
        newGui.currentState = this.currentState;
        newGui.selectedEnchantment = this.selectedEnchantment;
        newGui.itemToEnchant = this.inventory.getItem(ITEM_SLOT); // Get the most current item

        // 3. Open the new GUI. This forces a title and content update.
        newGui.open();
    }

    private void applyEnchantment(int level, int xpCost) {
        ItemStack itemToApplyTo = inventory.getItem(ITEM_SLOT);

        // --- Safety Checks (remain the same) ---
        if (itemToApplyTo == null || itemToApplyTo.getType() == Material.AIR) {
            player.sendMessage(ChatUtils.format("<red>The item was removed from the enchanting table!</red>"));
            player.closeInventory();
            return;
        }
        if (player.getLevel() < xpCost) {
            player.sendMessage(ChatUtils.format("<red>You don't have enough experience levels!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // --- Application Logic (remains the same) ---
        EnchantmentManager em = plugin.getEnchantmentManager();
        Map<String, Integer> currentEnchants = em.getEnchantments(itemToApplyTo);

        for (String incompatibleId : selectedEnchantment.getIncompatibilities()) {
            if (currentEnchants.containsKey(incompatibleId.toLowerCase())) {
                em.removeEnchantment(itemToApplyTo, incompatibleId);
            }
        }

        em.addEnchantment(itemToApplyTo, selectedEnchantment.getId(), level);
        player.setLevel(player.getLevel() - xpCost);
        plugin.getSkillManager().addXp(player, Skill.ENCHANTING, xpCost * 5.0);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);

        // 1. Set the state back to selecting an enchantment.
        this.currentState = ViewState.SELECTING_ENCHANTMENT;
        this.selectedEnchantment = null;

        // 2. Simply re-populate the items in the CURRENT inventory.
        //    This redraws the GUI in place, without closing and re-opening the window.
        //    It will automatically switch to the enchantment list view.
        populateItems();
    }

    private String toRoman(int number) {
        if (number < 1 || number > 10) return String.valueOf(number);
        String[] numerals = {"X", "IX", "V", "IV", "I"};
        int[] values = {10, 9, 5, 4, 1};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                result.append(numerals[i]);
            }
        }
        return result.toString();
    }
}