package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.EnchantmentManager;
import org.nakii.mmorpg.managers.SkillManager;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantingGui extends AbstractGui {

    private enum ViewState {
        SELECTING_ENCHANTMENT,
        SELECTING_LEVEL
    }

    private ViewState currentState = ViewState.SELECTING_ENCHANTMENT;
    private CustomEnchantment selectedEnchantment = null;
    private ItemStack itemToEnchant;
    private final Block enchantingTable; // NEW: Store the actual table block
    private int bookshelfPower = 0;      // NEW: Store calculated power

    private List<CustomEnchantment> applicableEnchants = new ArrayList<>();
    private static final int ITEM_SLOT = 19;

    // UPDATED: Main constructor now requires the block
    public EnchantingGui(MMORPGCore plugin, Player player, Block enchantingTable) {
        super(plugin, player);
        this.enchantingTable = enchantingTable;
        this.maxItemsPerPage = 15;
    }

    // UPDATED: Internal constructor for state changes
    private EnchantingGui(MMORPGCore plugin, Player player, Block enchantingTable, ItemStack itemToEnchant) {
        this(plugin, player, enchantingTable);
        this.itemToEnchant = itemToEnchant;
    }


    // NEW: Calculates the bookshelf power around the enchanting table
    private void calculateBookshelfPower() {
        this.bookshelfPower = 0;
        if (enchantingTable == null || enchantingTable.getType() != Material.ENCHANTING_TABLE) {
            return;
        }

        Block tableBase = enchantingTable.getLocation().getBlock();

        // Scan a 5x5 area on two levels (y=0 and y=1 relative to the table)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue; // Skip the table's own column
                for (int y = 0; y <= 1; y++) {
                    Block relative = tableBase.getRelative(x, y, z);
                    // Check for bookshelves in a 2-block high radius
                    if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                        if (relative.getType() == Material.BOOKSHELF) {
                            this.bookshelfPower++;
                        }
                        // TODO: Add support for "Enchanted Bookshelves" here if needed
                    }
                }
            }
        }
        // As per your docs, max power from regular shelves is 20.
        // We can let it go higher and cap it later if needed.
    }


    @Override
    public String getTitle() {
        if (currentState == ViewState.SELECTING_LEVEL && selectedEnchantment != null) {
            return "<dark_gray>" + selectedEnchantment.getDisplayName();
        }
        return "<dark_purple><b>Custom Enchanting</b></dark_purple>";
    }

    @Override
    public int getSize() { return 54; }

    @Override
    public void populateItems() {
        ItemStack currentItem = (this.itemToEnchant != null) ? this.itemToEnchant : inventory.getItem(ITEM_SLOT);
        this.itemToEnchant = null;

        inventory.clear();
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) { inventory.setItem(i, filler); }

        inventory.setItem(ITEM_SLOT, currentItem);
        inventory.setItem(28, createItem(Material.ENCHANTING_TABLE, "<light_purple>Place Item Above</light_purple>"));
        calculateBookshelfPower();
        inventory.setItem(48, createItem(Material.BOOKSHELF, "<gold>Bookshelf Power: <yellow>" + this.bookshelfPower + "</yellow></gold>"));
        inventory.setItem(49, createItem(Material.BARRIER, "<red><b>Close</b></red>"));
        inventory.setItem(50, createItem(Material.BOOK, "<green>Enchantment Guide</green>"));

        if (currentState == ViewState.SELECTING_ENCHANTMENT) {
            drawEnchantmentSelection(currentItem);
        } else if (currentState == ViewState.SELECTING_LEVEL) {
            drawLevelSelection();
        }
    }

    private void drawEnchantmentSelection(ItemStack itemToEnchant) {
        // This method remains largely the same
        if (itemToEnchant == null || itemToEnchant.getType() == Material.AIR) {
            applicableEnchants.clear();
            totalPages = 1; page = 0;
            inventory.setItem(22, createItem(Material.GRAY_DYE, "<gray>Applicable Enchantments", List.of("<dark_gray>Place an item to see available options.</dark_gray>")));
        } else {
            applicableEnchants = plugin.getEnchantmentManager().getApplicableEnchantments(itemToEnchant);
            totalPages = (int) Math.ceil((double) applicableEnchants.size() / maxItemsPerPage);

            int[] enchantSlots = {12, 13, 14, 15, 16, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34};
            int startIndex = page * maxItemsPerPage;

            for (int i = 0; i < maxItemsPerPage; i++) {
                int enchantIndex = startIndex + i;
                if (enchantIndex < applicableEnchants.size()) {
                    CustomEnchantment enchant = applicableEnchants.get(enchantIndex);
                    List<String> lore = new ArrayList<>();
                    lore.add(" ");
                    lore.addAll(enchant.getDescription(1));
                    lore.add(" ");
                    lore.add("<yellow>Click to select levels.</yellow>");
                    inventory.setItem(enchantSlots[i], createItem(Material.ENCHANTED_BOOK, "<green>" + enchant.getDisplayName() + "</green>", lore));
                } else {
                    inventory.setItem(enchantSlots[i], null); // Clear unused slots
                }
            }
        }

        if (page > 0) inventory.setItem(47, createItem(Material.PLAYER_HEAD, "<green>Previous Page</green>"));
        if (page < totalPages - 1) inventory.setItem(35, createItem(Material.PLAYER_HEAD, "<green>Next Page</green>"));
    }

    // --- MAJOR OVERHAUL of drawLevelSelection ---
    private void drawLevelSelection() {
        inventory.setItem(47, createItem(Material.ARROW, "<green>Go Back</green>"));

        int[] levelSlots = {20, 21, 22, 23, 24, 25, 26};
        SkillManager sm = plugin.getSkillManager();
        int playerSkillLevel = sm.getLevel(player, Skill.ENCHANTING);
        Map<String, Integer> currentEnchants = plugin.getEnchantmentManager().getEnchantments(inventory.getItem(ITEM_SLOT));
        int currentLevel = currentEnchants.getOrDefault(selectedEnchantment.getId(), 0);

        for (int i = 1; i <= selectedEnchantment.getMaxLevel(); i++) {
            if(i > levelSlots.length) break;

            int level = i;
            int cost = selectedEnchantment.getCost(level);
            int bookshelfReq = selectedEnchantment.getBookshelfRequirement(level);
            int skillReq = selectedEnchantment.getSkillRequirement(level);

            boolean canAfford = player.getLevel() >= cost;
            boolean hasBookshelves = this.bookshelfPower >= bookshelfReq;
            boolean hasSkill = playerSkillLevel >= skillReq;
            boolean canApply = canAfford && hasBookshelves && hasSkill;

            // UPDATED: ItemBuilder now uses a custom 'createItem' which handles MiniMessage
            String displayName = selectedEnchantment.getDisplayName() + " " + toRoman(level);
            List<String> lore = new ArrayList<>();

            if(level == currentLevel) {
                inventory.setItem(levelSlots[i-1], createItem(Material.DIAMOND, "<aqua><b>" + displayName + "</b></aqua>", List.of("<green>You already have this level.</green>")));
            } else if (level < currentLevel) {
                inventory.setItem(levelSlots[i-1], createItem(Material.GLASS_BOTTLE, "<gray>" + displayName + "</gray>", List.of("<dark_gray>This level is lower than your current one.</dark_gray>")));
            } else {
                lore.add(" ");
                lore.addAll(selectedEnchantment.getDescription(level));
                lore.add(" ");
                lore.add((canAfford ? "<gray>" : "<red>") + "Cost: <gold>" + cost + " XP Levels</gold>");
                lore.add((hasBookshelves ? "<gray>" : "<red>") + "Requires: <gold>" + bookshelfReq + " Bookshelf Power</gold>");
                lore.add((hasSkill ? "<gray>" : "<red>") + "Requires: <gold>Enchanting Skill " + skillReq + "</gold>");

                if(canApply) {
                    lore.add(" ");
                    lore.add("<yellow>Click to apply!</yellow>");
                }
                inventory.setItem(levelSlots[i-1], createItem(Material.EXPERIENCE_BOTTLE, (canApply ? "<green>" : "<red>") + displayName, lore));
            }
        }

    }


    @Override
    public void handleClick(InventoryClickEvent event) {
        // This part remains mostly the same, forwarding to the correct handler
        if (clickedInPlayerInventory(event)) {
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
        if (slot == 50) { // Enchantment Guide button logic remains
            returnItem(inventory.getItem(ITEM_SLOT));
            new EnchantmentGuideGui(plugin, player, this.enchantingTable).open();
            return;
        }
        if (slot == 47 && page > 0) { previousPage(); return; }
        if (slot == 35 && page < totalPages - 1) { nextPage(); return; }

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
        if (slot == 50) { // Enchantment Guide button logic remains
            returnItem(inventory.getItem(ITEM_SLOT));
            new EnchantmentGuideGui(plugin, player, this.enchantingTable).open();
            return;
        }

        int[] levelSlots = {20, 21, 22, 23, 24, 25, 26};
        for (int i = 0; i < levelSlots.length; i++) {
            if (slot == levelSlots[i]) {
                int level = i + 1;
                if(level > selectedEnchantment.getMaxLevel()) return;
                applyEnchantment(level); // Pass level to the validation method
                return;
            }
        }
    }

    private void openAsStateChange() {
        // Use the internal constructor to carry over the state
        EnchantingGui newGui = new EnchantingGui(plugin, player, this.enchantingTable, this.inventory.getItem(ITEM_SLOT));
        newGui.currentState = this.currentState;
        newGui.selectedEnchantment = this.selectedEnchantment;
        newGui.open();
    }


    // --- MAJOR OVERHAUL of applyEnchantment ---
    private void applyEnchantment(int level) {
        ItemStack itemToApplyTo = inventory.getItem(ITEM_SLOT);
        if (itemToApplyTo == null || itemToApplyTo.getType() == Material.AIR) {
            player.sendMessage(ChatUtils.format("<red>The item was removed from the enchanting table!</red>"));
            player.closeInventory();
            return;
        }

        // --- SERVER-SIDE VALIDATION ---
        Map<String, Integer> currentEnchants = plugin.getEnchantmentManager().getEnchantments(itemToApplyTo);
        int currentLevel = currentEnchants.getOrDefault(selectedEnchantment.getId(), 0);

        if (level <= currentLevel) {
            player.sendMessage(ChatUtils.format("<red>You cannot apply a lower or equal level enchantment.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int cost = selectedEnchantment.getCost(level);
        if (player.getLevel() < cost) {
            player.sendMessage(ChatUtils.format("<red>You don't have enough experience levels!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int bookshelfReq = selectedEnchantment.getBookshelfRequirement(level);
        if (this.bookshelfPower < bookshelfReq) {
            player.sendMessage(ChatUtils.format("<red>You don't have enough bookshelf power!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int skillReq = selectedEnchantment.getSkillRequirement(level);
        if (plugin.getSkillManager().getLevel(player, Skill.ENCHANTING) < skillReq) {
            player.sendMessage(ChatUtils.format("<red>Your Enchanting skill is too low!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // --- All checks passed, apply the enchantment ---
        EnchantmentManager em = plugin.getEnchantmentManager();
        for (String incompatibleId : selectedEnchantment.getIncompatibilities()) {
            if (currentEnchants.containsKey(incompatibleId.toLowerCase())) {
                em.removeEnchantment(itemToApplyTo, incompatibleId);
            }
        }

        em.addEnchantment(itemToApplyTo, selectedEnchantment.getId(), level);
        player.setLevel(player.getLevel() - cost);
        plugin.getSkillManager().addXp(player, Skill.ENCHANTING, cost * 5.0); // Grant skill XP
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);

        // Reset state and redraw the GUI to the enchantment list
        this.currentState = ViewState.SELECTING_ENCHANTMENT;
        this.selectedEnchantment = null;
        populateItems();
    }

    // Helper method to give item back to player
    private void returnItem(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            inventory.setItem(ITEM_SLOT, null); // Clear from GUI
            if (!player.getInventory().addItem(item).isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
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