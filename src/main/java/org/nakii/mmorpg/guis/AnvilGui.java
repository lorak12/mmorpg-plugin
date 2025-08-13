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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnvilGui extends AbstractGui {

    private static final int INPUT_LEFT_SLOT = 29;
    private static final int INPUT_RIGHT_SLOT = 33;
    private static final int RESULT_PREVIEW_SLOT = 13;
    private static final int COMBINE_BUTTON_SLOT = 22;
    private static final int CLOSE_BUTTON_SLOT = 49;

    private record CombinationResult(ItemStack item, int cost) {}
    private CombinationResult currentResult = null;

    public AnvilGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public @NotNull String getTitle() {
        return "<dark_gray><b>Custom Anvil</b></dark_gray>";
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public void populateItems() {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        // --- FIX: Only draw the frame, leave input slots empty ---
        for (int i = 0; i < getSize(); i++) {
            // Skip the slots that should be empty for the player to use.
            if (i == INPUT_LEFT_SLOT || i == INPUT_RIGHT_SLOT) {
                continue;
            }
            inventory.setItem(i, filler);
        }

        // Set the initial UI state (placeholders, buttons)
        updateResultUI();
        inventory.setItem(CLOSE_BUTTON_SLOT, createItem(Material.BARRIER, "<red><b>Close</b></red>"));
    }

    /**
     * --- THIS METHOD CONTAINS THE FIX FOR THE SHIFT-CLICK BUG ---
     */
    @Override
    public void handleClick(InventoryClickEvent event) {
        // We only want to cancel events for clicks inside the GUI.
        // Clicks in the player's inventory should be allowed to let shift-clicking work.
        if (clickedInPlayerInventory(event)) {
            // A shift-click will move an item. We need to update our GUI in response.
            // We schedule this for the next tick to give the server time to process the move.
            if (event.isShiftClick()) {
                plugin.getServer().getScheduler().runTask(plugin, this::updateResultUI);
            }
            return; // Do not cancel the event.
        }

        // The click is inside our GUI.
        int slot = event.getSlot();

        // Allow players to place and take items from the input slots.
        if (slot == INPUT_LEFT_SLOT || slot == INPUT_RIGHT_SLOT) {
            // Schedule an update to react to the item change.
            plugin.getServer().getScheduler().runTask(plugin, this::updateResultUI);
            return; // Do not cancel the event.
        }

        // For all other slots (fillers, buttons, result), we cancel the event.
        event.setCancelled(true);

        // Check if a button was clicked.
        if (slot == COMBINE_BUTTON_SLOT) {
            handleCombine();
        } else if (slot == CLOSE_BUTTON_SLOT) {
            player.closeInventory();
        }
    }

    private void handleCombine() {
        if (currentResult == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        if (player.getLevel() < currentResult.cost()) {
            player.sendMessage(ChatUtils.format("<red>You don't have enough experience levels!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        player.setLevel(player.getLevel() - currentResult.cost());
        inventory.setItem(INPUT_LEFT_SLOT, null);
        inventory.setItem(INPUT_RIGHT_SLOT, null);
        player.getInventory().addItem(currentResult.item());
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);

        this.currentResult = null;
        updateResultUI();
    }

    private void updateResultUI() {
        ItemStack baseItem = inventory.getItem(INPUT_LEFT_SLOT);
        ItemStack sacrificeItem = inventory.getItem(INPUT_RIGHT_SLOT);

        if (baseItem == null || sacrificeItem == null) {
            this.currentResult = null;
            setInactiveUI();
            return;
        }

        this.currentResult = calculateCombination(baseItem, sacrificeItem);

        if (this.currentResult != null) {
            setActiveUI(this.currentResult);
        } else {
            setInactiveUI();
        }
    }

    private void setActiveUI(CombinationResult result) {
        ItemStack greenPane = createItem(Material.GREEN_STAINED_GLASS_PANE, " ");
        inventory.setItem(11, greenPane); inventory.setItem(12, greenPane);
        inventory.setItem(14, greenPane); inventory.setItem(15, greenPane);
        inventory.setItem(20, greenPane); inventory.setItem(24, greenPane);

        inventory.setItem(COMBINE_BUTTON_SLOT, createItem(
                Material.ANVIL, "<green><b>Combine Items</b></green>",
                List.of("<gray>Cost: <yellow>" + result.cost() + " XP Levels</yellow>", " ", "<yellow>Click to combine!</yellow>")
        ));

        inventory.setItem(RESULT_PREVIEW_SLOT, result.item());
    }

    private void setInactiveUI() {
        // Using your preferred layout for the inactive state
        inventory.setItem(11, createItem(Material.RED_STAINED_GLASS_PANE, "<red><b>Item to Upgrade</b></red>", List.of("<gray>The item you want to upgrade should be placed below.</gray>")));
        inventory.setItem(12, createItem(Material.RED_STAINED_GLASS_PANE, "<red><b>Item to Upgrade</b></red>", List.of("<gray>The item you want to upgrade should be placed below.</gray>")));
        inventory.setItem(14, createItem(Material.RED_STAINED_GLASS_PANE, "<red><b>Item to Sacrifice</b></red>", List.of("<gray>The item you are sacrificing should be placed below.</gray>")));
        inventory.setItem(15, createItem(Material.RED_STAINED_GLASS_PANE, "<red><b>Item to Sacrifice</b></red>", List.of("<gray>The item you are sacrificing should be placed below.</gray>")));
        inventory.setItem(20, createItem(Material.RED_STAINED_GLASS_PANE, "<red><b>Item to Upgrade</b></red>", List.of("<gray>The item you want to upgrade should be placed below.</gray>")));
        inventory.setItem(24, createItem(Material.RED_STAINED_GLASS_PANE, "<red><b>Item to Sacrifice</b></red>", List.of("<gray>The item you are sacrificing should be placed below.</gray>")));

        inventory.setItem(COMBINE_BUTTON_SLOT, createItem(Material.ANVIL, "<gray>Combine Items</gray>", List.of("<red>Place two valid items in the slots to combine.</red>")));

        inventory.setItem(RESULT_PREVIEW_SLOT, createItem(Material.BARRIER, "<red>Cannot be combined!</red>"));
    }

    private CombinationResult calculateCombination(ItemStack base, ItemStack sacrifice) {
        // This calculation logic is correct and remains unchanged.
        EnchantmentManager em = plugin.getEnchantmentManager();
        Map<String, Integer> baseEnchants = new HashMap<>(em.getEnchantments(base));
        Map<String, Integer> sacrificeEnchants = em.getEnchantments(sacrifice);
        int totalLevelsAdded = 0;

        for (Map.Entry<String, Integer> entry : sacrificeEnchants.entrySet()) {
            String enchantId = entry.getKey();
            int sacrificeLevel = entry.getValue();
            CustomEnchantment enchant = em.getEnchantment(enchantId);
            if (enchant == null) continue;

            boolean isIncompatible = false;
            for (String incompatibleId : enchant.getIncompatibilities()) {
                if (baseEnchants.containsKey(incompatibleId)) {
                    isIncompatible = true;
                    break;
                }
            }
            if (isIncompatible) continue;

            int baseLevel = baseEnchants.getOrDefault(enchantId, 0);

            if (baseLevel > sacrificeLevel) continue;
            else if (baseLevel == sacrificeLevel) {
                int newLevel = baseLevel + 1;
                if (newLevel <= enchant.getMaxLevel()) {
                    baseEnchants.put(enchantId, newLevel);
                    totalLevelsAdded += newLevel;
                }
            } else {
                baseEnchants.put(enchantId, sacrificeLevel);
                totalLevelsAdded += sacrificeLevel;
            }
        }

        if (totalLevelsAdded == 0) return null;

        ItemStack resultItem = base.clone();
        em.setEnchantments(resultItem, baseEnchants);
        int cost = totalLevelsAdded * 5;
        return new CombinationResult(resultItem, cost);
    }

    private boolean clickedInPlayerInventory(InventoryClickEvent event) {
        return event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Player;
    }
}