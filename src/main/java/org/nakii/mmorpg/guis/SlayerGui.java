package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.SlayerManager;
import org.nakii.mmorpg.slayer.ActiveSlayerQuest;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SlayerGui extends AbstractGui {

    private enum ViewState { MAIN_MENU, TIER_SELECTION }
    private ViewState currentState = ViewState.MAIN_MENU;
    private String selectedSlayerType = null;

    public SlayerGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public @NotNull String getTitle() {
        if (currentState == ViewState.TIER_SELECTION && selectedSlayerType != null) {
            String displayName = plugin.getSlayerManager().getSlayerConfig().getString(selectedSlayerType + ".display-name", "Slayer");
            return displayName;
        }
        return "<dark_gray><b>Maddox the Slayer</b></dark_gray>";
    }

    @Override
    public int getSize() {
        return 54; // 6 rows
    }

    @Override
    public void populateItems() {
        // Standard frame
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) { inventory.setItem(i, filler); }

        // Back/Close button
        if (currentState == ViewState.TIER_SELECTION) {
            inventory.setItem(49, createItem(Material.ARROW, "<green>Go Back</green>"));
        } else {
            inventory.setItem(49, createItem(Material.BARRIER, "<red><b>Close</b></red>"));
        }

        // Display current quest info if one is active
        displayActiveQuestInfo();

        if (currentState == ViewState.MAIN_MENU) {
            drawMainMenu();
        } else if (currentState == ViewState.TIER_SELECTION) {
            drawTierSelection();
        }
    }

    private void drawMainMenu() {
        // This dynamically draws an icon for each slayer type defined in slayers.yml
        Set<String> slayerTypes = plugin.getSlayerManager().getSlayerConfig().getKeys(false);
        int[] slots = {20, 22, 24}; // Example slots
        int i = 0;

        for (String slayerId : slayerTypes) {
            if (i >= slots.length) break;

            String displayName = plugin.getSlayerManager().getSlayerConfig().getString(slayerId + ".display-name");
            List<String> lore = List.of(
                    "<gray>Slay mobs of this type",
                    "<gray>to spawn and defeat a powerful boss.",
                    " ",
                    "<yellow>Click to view tiers!</yellow>"
            );
            // In a full system, the material would be in the config
            Material iconMaterial = Material.ZOMBIE_HEAD;
            if(slayerId.contains("SPIDER")) iconMaterial = Material.SPIDER_EYE;

            inventory.setItem(slots[i], createItem(iconMaterial, displayName, lore));
            i++;
        }
    }

    private void drawTierSelection() {
        ConfigurationSection tiers = plugin.getSlayerManager().getSlayerConfig().getConfigurationSection(selectedSlayerType + ".tiers");
        if (tiers == null) return;

        int[] slots = {20, 22, 24, 26}; // Tiers I, II, III, IV
        int i = 0;

        for (String tierKey : tiers.getKeys(false)) {
            if (i >= slots.length) break;
            int tier = Integer.parseInt(tierKey);

            ConfigurationSection tierConfig = tiers.getConfigurationSection(tierKey);
            int cost = tierConfig.getInt("start-cost");
            int xp = tierConfig.getInt("xp-to-spawn");
            String bossId = tierConfig.getString("boss-id");

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Boss: <red>" + bossId.replace("_", " ") + "</red>");
            lore.add("<gray>XP to Spawn: <green>" + xp + "</green>");
            lore.add(" ");
            lore.add("<gray>Cost: <gold>" + cost + " Coins</gold>");
            lore.add(" ");
            lore.add("<yellow>Click to start quest!</yellow>");

            inventory.setItem(slots[i], createItem(Material.ENDER_EYE, "<green><b>Tier " + toRoman(tier) + "</b></green>", lore));
            i++;
        }
    }

    private void displayActiveQuestInfo() {
        SlayerManager sm = plugin.getSlayerManager();
        if (sm.hasActiveQuest(player)) {
            ActiveSlayerQuest quest = sm.getActiveQuest(player);
            String displayName = sm.getSlayerConfig().getString(quest.getSlayerType() + ".display-name");
            List<String> lore = List.of(
                    "<gray>Tier: <yellow>" + toRoman(quest.getTier()) + "</yellow>",
                    "<gray>Progress: <green>" + String.format("%.0f", quest.getCurrentXp()) + " / " + quest.getXpToSpawn() + "</green>",
                    " ",
                    "<red>You must cancel or complete this",
                    "<red>quest before starting a new one."
            );
            inventory.setItem(4, createItem(Material.BOOK, "<gold><b>Active Quest</b></gold>", lore));
        } else {
            inventory.setItem(4, createItem(Material.BOOK, "<green>No Active Quest</green>", List.of("<gray>Select a slayer to begin!</gray>")));
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();

        if (slot == 49) {
            if (currentState == ViewState.TIER_SELECTION) {
                currentState = ViewState.MAIN_MENU;
                selectedSlayerType = null;
                populateItems();
            } else {
                player.closeInventory();
            }
            return;
        }

        if (plugin.getSlayerManager().hasActiveQuest(player)) {
            player.sendMessage(ChatUtils.format("<red>You already have an active slayer quest!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (currentState == ViewState.MAIN_MENU) {
            handleMainMenuClick(slot);
        } else if (currentState == ViewState.TIER_SELECTION) {
            handleTierSelectionClick(slot);
        }
    }

    private void handleMainMenuClick(int slot) {
        Set<String> slayerTypes = plugin.getSlayerManager().getSlayerConfig().getKeys(false);
        int[] slots = {20, 22, 24};
        int i = 0;
        for (String slayerId : slayerTypes) {
            if (i >= slots.length) break;
            if (slot == slots[i]) {
                this.selectedSlayerType = slayerId;
                this.currentState = ViewState.TIER_SELECTION;
                populateItems();
                return;
            }
            i++;
        }
    }

    private void handleTierSelectionClick(int slot) {
        int[] slots = {20, 22, 24, 26};
        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i]) {
                int tier = i + 1;
                plugin.getSlayerManager().startQuest(player, selectedSlayerType, tier);
                player.closeInventory();
                return;
            }
        }
    }

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

    private boolean clickedInPlayerInventory(InventoryClickEvent event) {
        return event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Player;
    }
}