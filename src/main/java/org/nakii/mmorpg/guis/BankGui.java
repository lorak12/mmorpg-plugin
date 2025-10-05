package org.nakii.mmorpg.guis;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.economy.PlayerEconomy;
import org.nakii.mmorpg.economy.Transaction;
import org.nakii.mmorpg.events.PluginTimeUpdateEvent;
import org.nakii.mmorpg.managers.BankManager;
import org.nakii.mmorpg.managers.WorldTimeManager;
import org.nakii.mmorpg.util.ChatUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.kyori.adventure.audience.Audience;


public class BankGui extends AbstractGui {

    private enum ViewState { MAIN, DEPOSIT, WITHDRAW }
    private ViewState currentState = ViewState.MAIN;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    // --- Temporary sign state for "Specific Amount" input ---
    private static final Block pendingSignBlock = null;
    private static final BlockData previousBlockData = null;
    private static final ViewState pendingSignType = null;
    private static final UUID pendingSignPlayer = null;

    // --- THIS IS THE NEW DYNAMIC TIME LOGIC ---
    WorldTimeManager timeManager = plugin.getWorldTimeManager();

    // Calculate total days until the next season ends.
    int currentDayOfYear = (timeManager.getCurrentMonthOfYear() - 1) * WorldTimeManager.PLUGIN_DAYS_PER_MONTH + timeManager.getCurrentDayOfMonth();
    int currentSeasonIndex = (timeManager.getCurrentMonthOfYear() - 1) / WorldTimeManager.PLUGIN_MONTHS_PER_SEASON;
    int lastDayOfCurrentSeason = (currentSeasonIndex + 1) * WorldTimeManager.PLUGIN_MONTHS_PER_SEASON * WorldTimeManager.PLUGIN_DAYS_PER_MONTH;

    int daysLeft = lastDayOfCurrentSeason - currentDayOfYear;

    // Convert remaining days and current time into a total "plugin seconds left" value
    long secondsLeftInDay = WorldTimeManager.PLUGIN_SECONDS_PER_DAY -
            (((long) timeManager.getCurrentHour() * WorldTimeManager.PLUGIN_MINUTES_PER_HOUR) + timeManager.getCurrentMinute());

    long totalSecondsLeft = ((long) daysLeft * WorldTimeManager.PLUGIN_SECONDS_PER_DAY) + secondsLeftInDay;

    // Convert total plugin seconds into real-life hours and minutes for display
    long realMinutesLeft = (totalSecondsLeft / 60); // 1 plugin minute = 1 real second -> 60 plugin minutes = 1 real minute
    long realHoursLeft = realMinutesLeft / 60;
    long realMinutesLeftFormatted = realMinutesLeft % 60;

    String interestTime = String.format("%dh %dm", realHoursLeft, realMinutesLeftFormatted);


    public BankGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public @NotNull String getTitle() {
        return switch (currentState) {
            case DEPOSIT -> "<dark_gray><b>Bank Deposit</b></dark_gray>";
            case WITHDRAW -> "<dark_gray><b>Bank Withdrawal</b></dark_gray>";
            default -> "<dark_gray><b>⏣ Bank</b></dark_gray>";
        };
    }

    @Override
    public int getSize() {
        return 36; // 4 rows
    }

    @Override
    public void populateItems() {
        // Redraw the GUI based on the current state
        switch (currentState) {
            case DEPOSIT -> drawDepositMenu();
            case WITHDRAW -> drawWithdrawMenu();
            default -> drawMainMenu();
        }
    }

    private void drawMainMenu() {
        // --- Frame ---
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) { inventory.setItem(i, filler); }

        // --- Buttons ---
        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);
        BankManager bankManager = plugin.getBankManager();
        ConfigurationSection tierConfig = bankManager.getBankConfig().getConfigurationSection("tiers." + economy.getAccountTier());

        String balanceFormatted = formatter.format(Math.floor(economy.getBank()));
        String balanceLimitFormatted = "Infinite";
        if (tierConfig != null) {
            balanceLimitFormatted = formatter.format(tierConfig.getDouble("max-balance"));
        }

        // C - Deposit Button with updated time
        inventory.setItem(11, createItem(Material.CHEST, "<green>Deposit Coins</green>",
                List.of("<gray>Current balance: <gold>" + balanceFormatted + "</gold>",
                        "<gray>Interest in: <yellow>" + interestTime + "</yellow>", // <-- DYNAMIC VALUE
                        " ",
                        "<yellow>Click to make a deposit!</yellow>")));

        // D - Withdraw
        inventory.setItem(13, createItem(Material.DROPPER, "<green>Withdraw Coins</green>",
                List.of("<gray>Current balance: <gold>" + balanceFormatted + "</gold>", " ",
                        "<yellow>Click to withdraw coins!</yellow>")));

        // M - Transaction History
        List<String> historyLore = new ArrayList<>();
        historyLore.add("<gray>Your last 10 transactions.");
        historyLore.add(" ");
        for (Transaction tx : economy.getTransactionHistory()) {
            String type = tx.type() == Transaction.TransactionType.DEPOSIT ? "<green>+ " : "<red>- ";
            long millisAgo = System.currentTimeMillis() - tx.timestamp();
            String timeAgo = formatDuration(millisAgo);
            historyLore.add(type + "<gold>" + formatter.format(tx.amount()) + "</gold> <gray>(" + timeAgo + " ago)</gray>");
        }
        inventory.setItem(15, createItem(Material.MAP, "<green>Recent Transactions</green>", historyLore));

        // R - Information Button with updated time
        inventory.setItem(32, createItem(Material.REDSTONE_TORCH, "<red><b>Information</b></red>",
                List.of(
                        "<gray>Balance Limit: <gold>" + balanceLimitFormatted + "</gold>",
                        " ",
                        "<gray>The banker rewards you with interest",
                        "<gray>for coins in your bank balance.",
                        " ",
                        "<gray>Interest in: <yellow>" + interestTime + "</yellow>" // <-- DYNAMIC VALUE
                )
        ));

        // U - Upgrade Menu (conditional)
        if (economy.hasUnlockedUpgrades()) {
            inventory.setItem(35, createItem(Material.GOLD_BLOCK, "<gold><b>Bank Upgrades</b></gold>",
                    List.of("<gray>Current account: <green>" + (tierConfig != null ? tierConfig.getString("name") : "Unknown") + "</green>", // <-- FIX for Bug #4
                            "<gray>Bank limit: <gold>" + balanceLimitFormatted + "</gold>", // <-- FIX for Bug #4
                            " ",
                            "<yellow>Click to view upgrades!</yellow>")));
        }

        // X - Close
        inventory.setItem(31, createItem(Material.BARRIER, "<red><b>Close</b></red>"));
    }

    private void drawDepositMenu() {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) { inventory.setItem(i, filler); }

        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);

        // C - Deposit All
        inventory.setItem(11, createItem(Material.CHEST, "<green>Deposit entire purse</green>",
                List.of("<gray>Amount to deposit: <gold>" + formatter.format(economy.getPurse()) + "</gold>",
                        "<yellow>Click to deposit!</yellow>")));

        // D - Deposit Half
        inventory.setItem(13, createItem(Material.CHEST, "<green>Deposit half your purse</green>",
                List.of("<gray>Amount to deposit: <gold>" + formatter.format(economy.getPurse() / 2) + "</gold>",
                        "<yellow>Click to deposit!</yellow>")));

        // W - Specific Amount
        inventory.setItem(15, createItem(Material.OAK_SIGN, "<green>Specific Amount</green>",
                List.of("<gray>Click to specify an amount", "<gray>to deposit.</gray>")));

        // B - Back Button
        inventory.setItem(31, createItem(Material.ARROW, "<green>Go Back</green>"));
    }

    private void drawWithdrawMenu() {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) { inventory.setItem(i, filler); }

        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);

        // C - Withdraw All
        inventory.setItem(11, createItem(Material.DROPPER, "<green>Withdraw entire bank balance</green>",
                List.of("<gray>Amount to withdraw: <gold>" + formatter.format(economy.getBank()) + "</gold>",
                        "<yellow>Click to withdraw!</yellow>")));

        // D - Withdraw Half
        inventory.setItem(13, createItem(Material.DROPPER, "<green>Withdraw half your bank balance</green>",
                List.of("<gray>Amount to withdraw: <gold>" + formatter.format(economy.getBank() / 2) + "</gold>",
                        "<yellow>Click to withdraw!</yellow>")));

        // W - Specific Amount
        inventory.setItem(15, createItem(Material.OAK_SIGN, "<green>Specific Amount</green>",
                List.of("<gray>Click to specify an amount", "<gray>to withdraw.</gray>")));

        // B - Back Button
        inventory.setItem(31, createItem(Material.ARROW, "<green>Go Back</green>"));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        switch (currentState) {
            case DEPOSIT -> handleDepositClick(event);
            case WITHDRAW -> handleWithdrawClick(event);
            default -> handleMainClick(event);
        }
    }

    private void handleMainClick(InventoryClickEvent event) {
        switch (event.getSlot()) {
            case 11 -> { // Deposit
                currentState = ViewState.DEPOSIT;
                open();
            }
            case 13 -> { // Withdraw
                currentState = ViewState.WITHDRAW;
                open();
            }
            case 35 -> { // New Upgrade Slot
                if (plugin.getEconomyManager().getEconomy(player).hasUnlockedUpgrades()) {
                    new BankUpgradeGui(plugin, player).open();
                }
            }
            case 31 -> player.closeInventory();
        }
    }

    private void handleDepositClick(InventoryClickEvent event) {
        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);
        double amountToDeposit = 0;

        switch (event.getSlot()) {
            case 11 -> amountToDeposit = economy.getPurse(); // Deposit All
            case 13 -> amountToDeposit = economy.getPurse() / 2; // Deposit Half
            case 15 -> { // Specific Amount
                player.closeInventory(); // Close the GUI before showing the dialog
                openAmountDialog(true);
                return;
            }
            case 31 -> { // Back
                currentState = ViewState.MAIN;
                open();
                return;
            }
        }

        if (amountToDeposit > 0) {
            if (economy.deposit(amountToDeposit)) {
                player.sendMessage(ChatUtils.format("<green>You have deposited <gold>" + formatter.format(amountToDeposit) + " coins</gold>!</green>"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            } else {
                player.sendMessage(ChatUtils.format("<red>You don't have enough coins to deposit!</red>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
        // After any transaction, refresh the deposit menu to show the new purse balance
        populateItems();
    }

    private void handleWithdrawClick(InventoryClickEvent event) {
        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);
        double amountToWithdraw = 0;

        switch (event.getSlot()) {
            case 11 -> amountToWithdraw = economy.getBank(); // Withdraw All
            case 13 -> amountToWithdraw = economy.getBank() / 2; // Withdraw Half
            case 15 -> { // Specific Amount
                player.closeInventory();
                openAmountDialog(false);
                return;
            }
            case 31 -> { // Back
                currentState = ViewState.MAIN;
                open();
                return;
            }
        }

        if (amountToWithdraw > 0) {
            if (economy.withdraw(amountToWithdraw)) {
                player.sendMessage(ChatUtils.format("<green>You have withdrawn <gold>" + formatter.format(amountToWithdraw) + " coins</gold>!</green>"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            } else {
                player.sendMessage(ChatUtils.format("<red>You don't have enough coins in your bank to withdraw!</red>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
        // After any transaction, refresh the withdraw menu to show the new bank balance
        populateItems();
    }

    // Helper to format time for transaction history
    private String formatDuration(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        if (seconds < 60) return seconds + "s";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        if (minutes < 60) return minutes + "m";
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        if (hours < 24) return hours + "h";
        return TimeUnit.MILLISECONDS.toDays(millis) + "d";
    }

    // =========================================================================
    // Dialog-based amount input
    // =========================================================================

    /**
     * Open a Paper Dialog that contains a single text input named "amount".
     * The callback is registered inline via DialogAction.customClick and will
     * parse the entered text and call the appropriate deposit/withdraw handler.
     *
     * @param deposit true = this is a deposit dialog, false = withdraw dialog
     */
    private void openAmountDialog(boolean deposit) {
        // Build a dialog that contains a single text input called "amount".
        Dialog dialog = Dialog.create(builder -> builder
                .empty() // create a dialog on-the-fly (not registered in registry)
                .base(DialogBase.builder(Component.text(deposit ? "Deposit" : "Withdraw"))
                        .inputs(List.of(
                                // DialogInput.text(key, width, label, labelVisible, initial, maxLength, multilineOptions)
                                // width is an integer (1..1024). We use 200 as a reasonable width.
                                DialogInput.text("amount", 200, Component.text("Amount"), true, "", 12, null)
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        // Confirm button: parse and handle
                        ActionButton.create(
                                Component.text("Confirm"),
                                null,
                                120,
                                DialogAction.customClick((DialogResponseView view, Audience audience) -> {
                                    // Called when player clicks Confirm.
                                    // Read the text input and parse number.
                                    try {
                                        String raw = view.getText("amount"); // returns submitted text (nullable)
                                        if (raw == null || raw.isBlank()) {
                                            if (audience instanceof Player p) p.sendMessage(Component.text("You must enter a number."));
                                            return;
                                        }

                                        long value = Long.parseLong(raw.trim());
                                        if (value <= 0) {
                                            if (audience instanceof Player p) p.sendMessage(Component.text("Amount must be positive."));
                                            return;
                                        }

                                        if (audience instanceof Player p) {
                                            // call the same handlers you had before (they expect Player, long)
                                            if (deposit) {
                                                handleDeposit(p, value);
                                            } else {
                                                handleWithdraw(p, value);
                                            }
                                        }
                                    } catch (NumberFormatException nfe) {
                                        if (audience instanceof Player p) p.sendMessage(Component.text("Invalid number entered."));
                                    }
                                }, ClickCallback.Options.builder().uses(1).build())
                        ),
                        // Cancel button: just close / do nothing
                        ActionButton.create(
                                Component.text("Cancel"),
                                null,
                                120,
                                DialogAction.customClick((DialogResponseView view, Audience audience) -> {
                                    if (audience instanceof Player p) p.sendMessage(Component.text("Cancelled."));
                                }, ClickCallback.Options.builder().uses(1).build())
                        )
                ))
        );

        // Show the dialog to the player (Paper extension).
        player.showDialog(dialog);
    }

    // -- existing helpers (unchanged) --

    private void handleDeposit(Player player, long amount) {
        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);
        if (economy.deposit(amount)) {
            player.sendMessage(ChatUtils.format("<green>You have deposited <gold>" + formatter.format(amount) + " coins</gold>!</green>"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
        } else {
            player.sendMessage(ChatUtils.format("<red>You don't have enough coins to deposit!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    private void handleWithdraw(Player player, long amount) {
        PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);
        if (economy.withdraw(amount)) {
            player.sendMessage(ChatUtils.format("<green>You have withdrawn <gold>" + formatter.format(amount) + " coins</gold>!</green>"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
        } else {
            player.sendMessage(ChatUtils.format("<red>You don't have enough coins in your bank to withdraw!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    ///  Intrest part
    /**
     * --- THIS IS THE UPDATED METHOD ---
     * Listens for the plugin's 10-minute time update event.
     * It checks if the new time marks the end of a season and, if so,
     * triggers the interest payout for all online players.
     */
    @EventHandler
    public void onTimeUpdate(PluginTimeUpdateEvent event) {

        // We only need to check once per day, at the beginning of the day (midnight).
        if (event.getHour() == 0 && event.getMinute() == 0) {
            // Check if yesterday was the last day of the season.
            // (We check for the start of a new day to process the end of the previous one).
            if (timeManager.isLastDayOfSeason()) {
                applyEndOfSeasonInterest();
            }
        }
    }

    /**
     * Iterates through all online players and applies their calculated interest.
     */
    public void applyEndOfSeasonInterest() {
        plugin.getLogger().info("Applying end-of-season bank interest for all players...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerEconomy economy = plugin.getEconomyManager().getEconomy(player);
            BankManager bankManager = plugin.getBankManager();
            double interest = bankManager.calculateInterest(economy.getBank(), economy.getAccountTier());

            if (interest > 0) {
                economy.setBank(economy.getBank() + interest);
                economy.addTransaction(Transaction.TransactionType.INTEREST, interest);

                // Send the formatted in-game message
                player.sendMessage(ChatUtils.format(
                        "<green>You have just received <gold>" + formatter.format(interest) + " coins</gold> as interest in your personal bank account!</green>"
                ));
            }
        }
    }
}
