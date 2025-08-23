package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.collection.PlayerCollectionData;
import org.nakii.mmorpg.guis.AbstractGui;
import org.nakii.mmorpg.guis.CollectionMenu;
import org.nakii.mmorpg.managers.CollectionManager;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionsGui extends AbstractGui {

    private CollectionMenu currentMenu = CollectionMenu.MAIN_MENU;
    private String selectedCategory = null;
    private String selectedCollectionId = null;

    public CollectionsGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }

    public CollectionsGui(MMORPGCore plugin, Player player, CollectionMenu menu, String category, String collectionId) {
        super(plugin, player);
        this.currentMenu = menu;
        this.selectedCategory = category;
        this.selectedCollectionId = collectionId;
    }

    @Override
    public @NotNull String getTitle() {
        return switch (currentMenu) {
            case MAIN_MENU -> "Collections";
            case CATEGORY_VIEW -> selectedCategory.substring(0, 1) + selectedCategory.substring(1).toLowerCase() + " Collections";
            case COLLECTION_VIEW -> plugin.getCollectionManager().getCollectionConfig(selectedCollectionId).getString("display-name", "Collection");
        };
    }

    @Override
    public int getSize() { return 54; }

    @Override
    public void populateItems() {
        drawBaseLayout();
        switch (currentMenu) {
            case MAIN_MENU -> drawMainMenu();
            case CATEGORY_VIEW -> drawCategoryView();
            case COLLECTION_VIEW -> drawCollectionView();
        }
    }

    private void drawMainMenu() {
        inventory.setItem(11, createItem(Material.WHEAT, "<green>Farming Collections</green>", List.of("<gray>View your progress in all", "<gray>Farming-related collections.")));
        inventory.setItem(13, createItem(Material.DIAMOND_PICKAXE, "<blue>Mining Collections</blue>", List.of("<gray>View your progress in all", "<gray>Mining-related collections.")));
        inventory.setItem(15, createItem(Material.IRON_SWORD, "<red>Combat Collections</red>", List.of("<gray>View your progress in all", "<gray>Combat-related collections.")));
        inventory.setItem(21, createItem(Material.OAK_SAPLING, "<gold>Foraging Collections</gold>", List.of("<gray>View your progress in all", "<gray>Foraging-related collections.")));
        inventory.setItem(23, createItem(Material.FISHING_ROD, "<aqua>Fishing Collections</aqua>", List.of("<gray>View your progress in all", "<gray>Fishing-related collections.")));
    }

    private void drawCategoryView() {
        PlayerCollectionData data = plugin.getCollectionManager().getData(player);
        List<Map.Entry<String, YamlConfiguration>> collections = plugin.getCollectionManager().getCollectionsByCategory(selectedCategory);

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < collections.size() && i < slots.length; i++) {
            Map.Entry<String, YamlConfiguration> entry = collections.get(i);
            String collectionId = entry.getKey();
            YamlConfiguration config = entry.getValue();

            int amount = data.getProgress(collectionId);
            String displayName = config.getString("display-name");
            Material material = Material.matchMaterial(config.getString("material", "BARRIER"));

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Collected: <yellow>" + String.format("%,d", amount) + "</yellow>");
            lore.add(" ");
            // You can add progress bar to next tier here if desired
            lore.add("<yellow>Click to view tiers!</yellow>");

            inventory.setItem(slots[i], createItem(material, displayName, lore));
        }
    }

    private void drawCollectionView() {
        CollectionManager cm = plugin.getCollectionManager();
        PlayerCollectionData data = cm.getData(player);
        YamlConfiguration config = cm.getCollectionConfig(selectedCollectionId);
        if (config == null) return;

        inventory.setItem(4, createItem(Material.matchMaterial(config.getString("material")), config.getString("display-name")));

        int playerAmount = data.getProgress(selectedCollectionId);
        ConfigurationSection tiers = config.getConfigurationSection("tiers");
        if (tiers == null) return;

        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29}; // Tiers I-IX
        for (int i = 0; i < slots.length; i++) {
            int tier = i + 1;
            ConfigurationSection tierConfig = tiers.getConfigurationSection(String.valueOf(tier));
            if (tierConfig == null) continue;

            int required = tierConfig.getInt("required");
            boolean unlocked = playerAmount >= required;

            Material mat = unlocked ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String displayName = (unlocked ? "<green>" : "<red>") + "Tier " + toRoman(tier) + "</green>";

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Rewards:");
            tierConfig.getStringList("rewards").forEach(r -> lore.add("<gold>• <gray>" + r.replace("_", " ")));
            lore.add(" ");
            lore.add("<gray>Progress: <yellow>" + String.format("%,d", playerAmount) + " / " + String.format("%,d", required) + "</yellow>");
            lore.add(generateProgressBar(playerAmount, required));

            inventory.setItem(slots[i], createItem(mat, displayName, lore));
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();

        if (slot == 49) { // Back/Close button from AbstractGui
            handleBackClick();
            return;
        }

        switch (currentMenu) {
            case MAIN_MENU -> handleMainMenuClick(slot);
            case CATEGORY_VIEW -> handleCategoryViewClick(slot);
        }
    }

    private void handleBackClick() {
        if (currentMenu == CollectionMenu.MAIN_MENU) {
            player.closeInventory();
        } else if (currentMenu == CollectionMenu.COLLECTION_VIEW) {
            this.currentMenu = CollectionMenu.CATEGORY_VIEW;
            reopen();
        } else {
            this.currentMenu = CollectionMenu.MAIN_MENU;
            reopen();
        }
    }

    private void handleMainMenuClick(int slot) {
        String category = switch (slot) {
            case 11 -> "FARMING";
            case 13 -> "MINING";
            case 15 -> "COMBAT";
            case 21 -> "FORAGING";
            case 23 -> "FISHING";
            default -> null;
        };
        if (category != null) {
            this.selectedCategory = category;
            this.currentMenu = CollectionMenu.CATEGORY_VIEW;
            reopen();
        }
    }

    private void handleCategoryViewClick(int slot) {
        List<Map.Entry<String, YamlConfiguration>> collections = plugin.getCollectionManager().getCollectionsByCategory(selectedCategory);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < collections.size() && i < slots.length; i++) {
            if (slot == slots[i]) {
                this.selectedCollectionId = collections.get(i).getKey();
                this.currentMenu = CollectionMenu.COLLECTION_VIEW;
                reopen();
                return;
            }
        }
    }

    // --- Utilities ---
    private void reopen() {
        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                new CollectionsGui(plugin, player, currentMenu, selectedCategory, selectedCollectionId).open();
            }
        }.runTaskLater(plugin, 1L);
    }

    private String generateProgressBar(int current, int max) {
        if (max <= 0) return "<dark_gray>-----------------</dark_gray>";
        float percent = Math.min(1.0f, (float) current / max);
        int greenChars = (int) (17 * percent);
        int grayChars = 17 - greenChars;
        return "<green>" + "-".repeat(greenChars) + "</green><dark_gray>" + "-".repeat(grayChars) + "</dark_gray>";
    }

    private String toRoman(int num) {
        if (num < 1 || num > 10) return String.valueOf(num);
        String[] roman = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return roman[num];
    }
}