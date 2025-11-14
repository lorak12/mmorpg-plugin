package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
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
import org.nakii.mmorpg.events.PluginTimeUpdateEvent;
import org.nakii.mmorpg.util.ChatUtils;

import java.io.File;
import java.util.*;

public class BankManager implements Listener {

    private final MMORPGCore plugin;
    private final EconomyManager economyManager;
    private final ItemManager itemManager;
    private final FileConfiguration bankConfig;

    public BankManager(MMORPGCore plugin, EconomyManager economyManager, ItemManager itemManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.itemManager = itemManager;
        this.bankConfig = loadBankConfig();
    }

    private FileConfiguration loadBankConfig() {
        File file = new File(plugin.getDataFolder(), "bank.yml");
        if (!file.exists()) {
            plugin.saveResource("bank.yml", false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

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

            double applicableBalance = Math.max(0, Math.min(balance, upTo) - balanceChecked);
            if (applicableBalance <= 0) break;

            totalInterest += applicableBalance * (rate / 100.0);
            balanceChecked = upTo;
        }

        return Math.min(totalInterest, maxInterest);
    }

    public void applyEndOfSeasonInterest() {
        plugin.getLogger().info("Applying end-of-season bank interest for all players...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerEconomy economy = economyManager.getEconomy(player);
            double interest = calculateInterest(economy.getBank(), economy.getAccountTier());

            if (interest > 0) {
                economy.setBank(economy.getBank() + interest);
                economy.addTransaction(Transaction.TransactionType.INTEREST, interest);
                player.sendMessage("You earned " + interest + " coins in interest!");
            }
        }
    }

    @EventHandler
    public void onTimeUpdate(PluginTimeUpdateEvent event) {
        if (event.getHour() == 0 && event.getMinute() == 0) {
            applyEndOfSeasonInterest();
        }
    }

    @EventHandler
    public void onBalanceChange(PlayerBalanceChangeEvent event) {
        Player player = event.getPlayer();
        PlayerEconomy economy = economyManager.getEconomy(player);

        if (economy.hasUnlockedUpgrades()) return;

        double requirement = bankConfig.getDouble("upgrade-unlock-requirement");
        if ((economy.getPurse() + economy.getBank()) >= requirement) {
            economy.setHasUnlockedUpgrades(true);
            player.sendMessage(ChatUtils.format("<green><b>Congratulations!</b> You have unlocked Bank Account Upgrades!</green>"));
        }
    }

    public void attemptUpgrade(Player player, String targetTierId) {
        PlayerEconomy economy = economyManager.getEconomy(player);
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

        if (economy.getPurse() < cost) {
            player.sendMessage(ChatUtils.format("<red>You don't have enough coins in your purse to afford this upgrade!</red>"));
            return;
        }

        ConfigurationSection itemCostsSection = targetTierConfig.getConfigurationSection("upgrade-cost-items");
        Map<String, Integer> requiredItems = new HashMap<>();
        if (itemCostsSection != null) {
            for (String key : itemCostsSection.getKeys(false)) {
                requiredItems.put(key, itemCostsSection.getInt(key));
            }
        }

        if (!hasAndRemoveRequiredItems(player, requiredItems)) {
            return;
        }

        economy.removePurse(cost);
        economy.setAccountTier(targetTierId);

        player.sendMessage(ChatUtils.format("<green><b>ACCOUNT UPGRADED!</b> You now have a <gold>" + targetTierConfig.getString("name") + "</gold>!</green>"));
    }

    private boolean hasAndRemoveRequiredItems(Player player, Map<String, Integer> requiredItems) {
        if (requiredItems.isEmpty()) {
            return true;
        }

        Map<String, Integer> playerItemCounts = new HashMap<>();
        for (ItemStack item : player.getInventory().getContents()) {
            String itemId = itemManager.getItemId(item);
            if (itemId != null && requiredItems.containsKey(itemId)) {
                playerItemCounts.put(itemId, playerItemCounts.getOrDefault(itemId, 0) + item.getAmount());
            }
        }

        for (Map.Entry<String, Integer> required : requiredItems.entrySet()) {
            String itemId = required.getKey();
            int requiredAmount = required.getValue();
            if (playerItemCounts.getOrDefault(itemId, 0) < requiredAmount) {
                player.sendMessage(ChatUtils.format("<red>You are missing required items for this upgrade! (Need: " + requiredAmount + "x " + itemId + ")</red>"));
                return false;
            }
        }

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