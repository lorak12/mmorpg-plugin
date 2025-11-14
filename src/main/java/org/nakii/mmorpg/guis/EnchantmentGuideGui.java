package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.EnchantmentManager;
import org.nakii.mmorpg.managers.SkillManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantmentGuideGui extends AbstractGui {

    private final List<CustomEnchantment> allEnchantments;
    private final Block enchantingTable;
    private final EnchantmentManager enchantmentManager;
    private final SkillManager skillManager;

    public EnchantmentGuideGui(MMORPGCore plugin, EnchantmentManager enchantmentManager, SkillManager skillManager, Player player, Block enchantingTable) {
        super(plugin, player);
        this.enchantingTable = enchantingTable; // <-- Store the passed block
        this.enchantmentManager = enchantmentManager;
        this.skillManager = skillManager;

        this.allEnchantments = enchantmentManager.getAllEnchantments().values()
                .stream()
                .sorted((e1, e2) -> e1.getDisplayName().compareToIgnoreCase(e2.getDisplayName()))
                .collect(Collectors.toList());

        this.maxItemsPerPage = 28;
        this.totalPages = (int) Math.ceil((double) this.allEnchantments.size() / this.maxItemsPerPage);
    }

    @Override
    public String getTitle() { return "<dark_purple><b>Enchantment Guide</b></dark_purple>"; }
    @Override
    public int getSize() { return 54; }

    @Override
    public void populateItems() {
        // Clear the content area first to prevent ghost items on page change
        for (int i = 10; i < 44; i++) {
            if (i % 9 != 0 && i % 9 != 8) {
                inventory.setItem(i, null);
            }
        }

        // Draw the standard layout (border and pagination buttons)
        drawBaseLayout();

        // --- Static Layout Items ---
        inventory.setItem(4, createItem(Material.BOOK, "<light_purple>All Enchantments</light_purple>", List.of("<gray>A comprehensive list of all","<gray>enchantments in the game.")));

        // --- Populate the page with enchantments ---
        int startIndex = page * maxItemsPerPage;
        int slotIndex = 0;

        // Define the slots available for content in our layout
        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = startIndex; i < startIndex + maxItemsPerPage; i++) {
            if (i >= this.allEnchantments.size()) break; // Stop if we've run out of enchants
            if (slotIndex >= contentSlots.length) break; // Stop if we've run out of slots

            CustomEnchantment enchant = this.allEnchantments.get(i);

            // Format the description with placeholder values replaced
            List<String> formattedDesc = new ArrayList<>();
            for(String line : enchant.getDescription()) {
                // Replace any placeholders with generic "X" for the guide
                line = line.replaceAll("\\{.*?\\}", "X");
                formattedDesc.add(line);
            }

            inventory.setItem(contentSlots[slotIndex], createItem(Material.ENCHANTED_BOOK, "<green>" + enchant.getDisplayName(), formattedDesc));
            slotIndex++;
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        super.handleClick(event); // Handles pagination and default cancellation
        int slot = event.getSlot();

        if (event.getClickedInventory() == inventory) {
            // FIX: The "Close" button now correctly opens the EnchantingGui WITH the block
            if (slot == 49) {
                if (this.enchantingTable != null) {
                    new EnchantingGui(plugin, player, this.enchantingTable, enchantmentManager, skillManager).open();
                } else {
                    player.closeInventory(); // Fallback if no table was provided
                }
            }
        }
    }
}