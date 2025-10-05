package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.economy.PlayerEconomy;
import org.nakii.mmorpg.managers.BankManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BankUpgradeGui extends AbstractGui {

    private final NumberFormat formatter = NumberFormat.getInstance();

    public BankUpgradeGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public @NotNull String getTitle() {
        return "<dark_gray><b>Bank Account Upgrades</b></dark_gray>";
    }

    @Override
    public int getSize() {
        return 36; // 4 rows
    }

    @Override
    public void populateItems() {
        // --- Frame ---
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) { inventory.setItem(i, filler); }

        // --- Back Button ---
        inventory.setItem(31, createItem(Material.ARROW, "<green>Go Back</green>"));

        // --- Dynamically generate an icon for each tier ---
        BankManager bankManager = plugin.getBankManager();
        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);

        // We get the tiers in the order they are defined in the config.
        Set<String> tierIds = bankManager.getBankConfig().getConfigurationSection("tiers").getKeys(false);
        int[] slots = {10, 11, 12, 13, 14, 15, 16}; // Slots for S, N, I, A, H, P, G
        int i = 0;

        String currentTierId = economy.getAccountTier();
        int currentTierIndex = new ArrayList<>(tierIds).indexOf(currentTierId);

        for (String tierId : tierIds) {
            if (i >= slots.length) break;

            ConfigurationSection tierConfig = bankManager.getBankConfig().getConfigurationSection("tiers." + tierId);

            String tierName = tierConfig.getString("name");
            Material icon = Material.BARRIER; // Default icon
            List<String> lore = new ArrayList<>();

            // Determine the player's status relative to this tier
            int tierIndex = new ArrayList<>(tierIds).indexOf(tierId);
            boolean isCurrent = tierId.equalsIgnoreCase(currentTierId);
            boolean isNext = (tierIndex == currentTierIndex + 1);

            // Set icon based on status
            if (isCurrent) {
                icon = Material.EMERALD_BLOCK;
                lore.add("<green><b>CURRENT ACCOUNT</b></green>");
            } else if (tierIndex < currentTierIndex) {
                icon = Material.LIME_STAINED_GLASS_PANE;
                lore.add("<gray>You have already unlocked this tier.</gray>");
            } else {
                icon = Material.RED_STAINED_GLASS_PANE;
                lore.add("<red>LOCKED</red>");
            }
            lore.add(" ");

            // Build the rest of the lore
            lore.add("<gray>Max Balance: <gold>" + formatter.format(tierConfig.getDouble("max-balance")) + " Coins</gold>");
            lore.add("<gray>Max Interest: <gold>" + formatter.format(tierConfig.getDouble("max-interest")) + " Coins</gold>");

            // Add interest tranches
            lore.add(" ");
            lore.add("<gray>>----- Interest Tranches -----<");
            tierConfig.getMapList("interest-tranches").forEach(tranche -> {
                lore.add(String.format("<gray>  Up to %,.0f Coins: <green>%.2f%%</green>",
                        ((Number) tranche.get("up-to")).doubleValue(),
                        ((Number) tranche.get("rate")).doubleValue()));
            });
            lore.add("<gray>>-------------------------<");

            // Add upgrade requirements for the next tier
            if (isNext) {
                lore.add(" ");
                lore.add("<white><b>Upgrade Requirements:</b></white>");

                // For now, these are placeholder checks. A real system needs a CollectionManager.
                boolean hasEnoughCoins = economy.getPurse() >= tierConfig.getDouble("upgrade-cost-coins");

                lore.add((hasEnoughCoins ? "<green>✔" : "<red>✖") + " <gray>Cost: <gold>" + formatter.format(tierConfig.getDouble("upgrade-cost-coins")) + " Coins</gold>");
                // Add checks for items and collections here...

                if(hasEnoughCoins) {
                    lore.add(" ");
                    lore.add("<yellow>Click to upgrade!</yellow>");
                }
            } else if (!isCurrent && tierIndex > currentTierIndex) {
                lore.add(" ");
                lore.add("<red>You must unlock previous tiers first.</red>");
            }


            inventory.setItem(slots[i], createItem(icon, "<gold><b>" + tierName + "</b></gold>", lore));
            i++;
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();

        if (slot == 31) { // Back button
            new BankGui(plugin, player).open();
            return;
        }

        // --- Handle clicking on an upgrade icon ---
        Set<String> tierIds = plugin.getBankManager().getBankConfig().getConfigurationSection("tiers").getKeys(false);
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int i = 0;
        for (String tierId : tierIds) {
            if (i >= slots.length) break;
            if (slot == slots[i]) {
                // Player clicked this tier, attempt to upgrade.
                plugin.getBankManager().attemptUpgrade(player, tierId);
                // Refresh the GUI to show the new status
                populateItems();
                return;
            }
            i++;
        }
    }
}