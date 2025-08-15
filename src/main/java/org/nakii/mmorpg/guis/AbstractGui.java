package org.nakii.mmorpg.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractGui implements InventoryHolder {

    public static final Map<UUID, AbstractGui> OPEN_GUIS = new HashMap<>();

    protected final MMORPGCore plugin;
    protected final Player player;
    protected Inventory inventory;

    protected int page = 0;
    protected int maxItemsPerPage;
    protected int totalPages = 1;

    public AbstractGui(MMORPGCore plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public abstract String getTitle();
    public abstract int getSize();
    public abstract void populateItems();

    public void open() {
        // Use ChatUtils to format the title string into a Component
        this.inventory = Bukkit.createInventory(this, getSize(), ChatUtils.format(getTitle()));
        this.populateItems();

        OPEN_GUIS.put(player.getUniqueId(), this);
        player.openInventory(this.inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == inventory) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 48) previousPage();
            else if (slot == 50) nextPage();
            else if (slot == 49) player.closeInventory();
        }
    }

    /**
     * Handles the logic for a shift-click from the player's inventory into this GUI.
     * @param clickedItem The item that was clicked.
     * @return The remainder of the item stack if it couldn't be fully moved.
     */
    public ItemStack handleShiftClick(ItemStack clickedItem) {
        // Default behavior: do nothing, return the itemstack unchanged.
        return clickedItem;
    }

    protected void previousPage() {
        if (page > 0) {
            page--;
            populateItems();
        }
    }

    protected void nextPage() {
        if (page < totalPages - 1) {
            page++;
            populateItems();
        }
    }

    @NotNull @Override
    public Inventory getInventory() { return inventory; }

    protected void drawBaseLayout() {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) {
            if (i < 9 || i > getSize() - 10 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, filler);
            }
        }
        if (page > 0) inventory.setItem(48, createItem(Material.ARROW, "<green>Previous Page</green>"));
        if (page < totalPages - 1) inventory.setItem(50, createItem(Material.ARROW, "<green>Next Page</green>"));
        inventory.setItem(49, createItem(Material.BARRIER, "<red><b>Close</b></red>"));
    }

    // --- THIS IS THE FIX ---
    /**
     * The main, definitive helper method for creating GUI items.
     * @param material The material of the item.
     * @param name The MiniMessage-formatted name.
     * @param lore A list of MiniMessage-formatted lore lines.
     * @return The created ItemStack.
     */
    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.format(name));
            if (lore != null && !lore.isEmpty()) {
                meta.lore(ChatUtils.formatList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * An overloaded helper for creating items with no lore.
     */
    protected ItemStack createItem(Material material, String name) {
        return createItem(material, name, Collections.emptyList());
    }
    // --- END OF FIX ---
}