package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.economy.PlayerEconomy;
import org.nakii.mmorpg.economy.Transaction;
import org.nakii.mmorpg.events.PlayerBalanceChangeEvent;
import org.nakii.mmorpg.events.PluginTimeUpdateEvent; // Assuming you have a general time event
import org.nakii.mmorpg.utils.ChatUtils;

import java.io.File;
import java.util.*;

public class BankManager implements Listener { // Implement Listener to hear our time event

    private final MMORPGCore plugin;



    private final FileConfiguration bankConfig;

    public BankManager(MMORPGCore plugin) {
        this.plugin = plugin;
        this.bankConfig = loadBankConfig();
        // Register this manager as a listener so it can hear events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private FileConfiguration loadBankConfig() {
        File file = new File(plugin.getDataFolder(), "bank.yml");
        if (!file.exists()) {
            plugin.saveResource("bank.yml", false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * The core interest calculation logic.
     * @param balance The current bank balance of the player.
     * @param tierId The ID of the player's account tier (e.g., "STARTER").
     * @return The total amount of interest earned.
     */
    public double calculateInterest(double balance, String tierId) {
        ConfigurationSection tierConfig = bankConfig.getConfigurationSection("tiers." + tierId);
        if (tierConfig == null) return 0.0;

        List<Map<?, ?>> tranches = tierConfig.getMapList("interest-tranches");
        double maxInterest = tierConfig.getDouble("max-interest");
        double totalInterest = 0;
        double balanceChecked = 0;

        for (Map<?, ?> tranche : tranches) {
            double upTo = ((Number) tranche.get("up-to")).doubleValue();
            double rate = ((Number) tranche.get("rate")).doubleValue();

            // Determine how much of the balance falls into this tranche
            double applicableBalance = Math.max(0, Math.min(balance, upTo) - balanceChecked);
            if (applicableBalance <= 0) break;

            totalInterest += applicableBalance * (rate / 100.0);
            balanceChecked = upTo;
        }

        return Math.min(totalInterest, maxInterest);
    }

    /**
     * This method is triggered when the plugin's internal clock detects a new season.
     */
    public void applyEndOfSeasonInterest() {
        plugin.getLogger().info("Applying end-of-season bank interest for all players...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);
            double interest = calculateInterest(economy.getBank(), economy.getAccountTier());

            if (interest > 0) {
                economy.setBank(economy.getBank() + interest);
                economy.addTransaction(Transaction.TransactionType.INTEREST, interest);
                // In a full system, you would send the formatted game message here.
                player.sendMessage("You earned " + interest + " coins in interest!");
            }
        }
    }

    // You can create a custom SeasonChangeEvent or use the existing time update event
    @EventHandler
    public void onTimeUpdate(PluginTimeUpdateEvent event) {
        // This is a placeholder for the "end of season" logic.
        // A full implementation would get the day/month from WorldTimeManager.
        // For now, let's trigger it at a specific time, e.g., midnight of day 1.
        if (event.getHour() == 0 && event.getMinute() == 0 /* && isLastDayOfSeason() */) {
            applyEndOfSeasonInterest();
        }
    }

    /**
     * --- THIS REPLACES THE OLD checkUpgradeUnlock METHOD ---
     * Listens for any balance change and checks if the player now qualifies
     * for unlocking the bank upgrade menu.
     */
    @EventHandler
    public void onBalanceChange(PlayerBalanceChangeEvent event) {
        Player player = event.getPlayer();
        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);

        if (economy.hasUnlockedUpgrades()) return;

        double requirement = bankConfig.getDouble("upgrade-unlock-requirement");
        if ((economy.getPurse() + economy.getBank()) >= requirement) {
            economy.setHasUnlockedUpgrades(true);
            player.sendMessage(ChatUtils.format("<green><b>Congratulations!</b> You have unlocked Bank Account Upgrades!</green>"));

        }
    }

    /**
     * --- THIS IS THE NEW METHOD ---
     * Attempts to upgrade a player's bank account to the target tier.
     * @param player The player attempting the upgrade.
     * @param targetTierId The ID of the tier they want to upgrade to (e.g., "GOLD").
     */
    public void attemptUpgrade(Player player, String targetTierId) {
        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);
        ConfigurationSection tiersConfig = bankConfig.getConfigurationSection("tiers");
        if (tiersConfig == null) return;

        List<String> tierOrder = new ArrayList<>(tiersConfig.getKeys(false));
        String currentTierId = economy.getAccountTier();

        int currentTierIndex = tierOrder.indexOf(currentTierId.toUpperCase());
        int targetTierIndex = tierOrder.indexOf(targetTierId.toUpperCase());

        if (targetTierIndex != currentTierIndex + 1) {
            player.sendMessage(ChatUtils.format(targetTierIndex <= currentTierIndex ? "<gray>You already have this or a better account.</gray>" : "<red>You must unlock the previous tiers first!</red>"));
            return;
        }

        ConfigurationSection targetTierConfig = tiersConfig.getConfigurationSection(targetTierId);
        double cost = targetTierConfig.getDouble("upgrade-cost-coins");

        // 1. Coin Check
        if (economy.getPurse() < cost) {
            player.sendMessage(ChatUtils.format("<red>You don't have enough coins in your purse to afford this upgrade!</red>"));
            return;
        }

        // 2. Item Check
        ConfigurationSection itemCostsSection = targetTierConfig.getConfigurationSection("upgrade-cost-items");
        Map<String, Integer> requiredItems = new HashMap<>();
        if (itemCostsSection != null) {
            for (String key : itemCostsSection.getKeys(false)) {
                requiredItems.put(key, itemCostsSection.getInt(key));
            }
        }

        // The hasAndRemove method will check for items and only remove them on success.
        // It returns false and sends a message to the player on failure.
        if (!hasAndRemoveRequiredItems(player, requiredItems)) {
            return;
        }

        // 3. Collection Check (still a placeholder)
        // ...

        // --- All checks passed, perform the upgrade ---
        economy.removePurse(cost);
        economy.setAccountTier(targetTierId);

        player.sendMessage(ChatUtils.format("<green><b>ACCOUNT UPGRADED!</b> You now have a <gold>" + targetTierConfig.getString("name") + "</gold>!</green>"));
        // Sounds, effects, etc.
    }

    /**
     * --- THIS IS THE NEW HELPER METHOD ---
     * Checks if a player has the required items and consumes them if they do.
     * @param player The player whose inventory to check.
     * @param requiredItems A map of Custom Item IDs and the required amount.
     * @return True if the player had all items and they were consumed, false otherwise.
     */
    private boolean hasAndRemoveRequiredItems(Player player, Map<String, Integer> requiredItems) {
        if (requiredItems.isEmpty()) {
            return true; // No items are required.
        }

        ItemManager itemManager = plugin.getItemManager();
        Map<String, Integer> playerItemCounts = new HashMap<>();

        // First pass: Count all relevant custom items in the player's inventory.
        for (ItemStack item : player.getInventory().getContents()) {
            String itemId = itemManager.getItemId(item);
            if (itemId != null && requiredItems.containsKey(itemId)) {
                playerItemCounts.put(itemId, playerItemCounts.getOrDefault(itemId, 0) + item.getAmount());
            }
        }

        // Second pass: Check if the player has enough of each required item.
        for (Map.Entry<String, Integer> required : requiredItems.entrySet()) {
            String itemId = required.getKey();
            int requiredAmount = required.getValue();
            if (playerItemCounts.getOrDefault(itemId, 0) < requiredAmount) {
                player.sendMessage(ChatUtils.format("<red>You are missing required items for this upgrade! (Need: " + requiredAmount + "x " + itemId + ")</red>"));
                return false;
            }
        }

        // Third pass: All checks passed, so now we consume the items.
        for (Map.Entry<String, Integer> toRemove : requiredItems.entrySet()) {
            int amountToRemove = toRemove.getValue();
            ItemStack itemToRemove = itemManager.createItemStack(toRemove.getKey());
            itemToRemove.setAmount(amountToRemove);
            player.getInventory().removeItem(itemToRemove);
        }

        return true;
    }

    public FileConfiguration getBankConfig() {
        return bankConfig;
    }


}