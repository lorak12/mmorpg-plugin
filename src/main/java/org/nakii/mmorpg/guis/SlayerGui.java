package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.EconomyManager;
import org.nakii.mmorpg.managers.RequirementManager;
import org.nakii.mmorpg.managers.SlayerDataManager;
import org.nakii.mmorpg.managers.SlayerManager;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.requirements.Requirement;
import org.nakii.mmorpg.slayer.PlayerSlayerData;
import org.nakii.mmorpg.util.ChatUtils;
import org.nakii.mmorpg.util.FormattingUtils;

import java.util.ArrayList;
import java.util.List;

public class SlayerGui extends AbstractGui {

    // We replace the ViewState enum with our new SlayerMenu enum
    private SlayerMenu currentMenu = SlayerMenu.MAIN_MENU;
    private String selectedSlayerType = null;
    private int selectedTier = -1; // To remember which tier is being confirmed
    private final SlayerManager slayerManager;
    private final SlayerDataManager slayerDataManager;
    private final EconomyManager economyManager;
    private final RequirementManager requirementManager;


    /**
     * The default constructor for opening the GUI from an external source,
     * like a command. It initializes the GUI to its default main menu state.
     */
    public SlayerGui(MMORPGCore plugin, Player player, SlayerManager slayerManager, SlayerDataManager slayerDataManager, EconomyManager economyManager, RequirementManager requirementManager) {
        super(plugin, player);
        this.slayerManager = slayerManager;
        this.slayerDataManager = slayerDataManager;
        this.economyManager = economyManager;
        this.requirementManager = requirementManager;
    }

    // Internal constructor for state changes
    public SlayerGui(MMORPGCore plugin, Player player, SlayerMenu currentMenu, String selectedSlayerType, int selectedTier, SlayerManager slayerManager, SlayerDataManager slayerDataManager, EconomyManager economyManager, RequirementManager requirementManager) {
        this(plugin, player, slayerManager, slayerDataManager, economyManager, requirementManager);
        this.currentMenu = currentMenu;
        this.selectedSlayerType = selectedSlayerType;
        this.selectedTier = selectedTier;
    }

    @Override
    public @NotNull String getTitle() {
        String title = switch (currentMenu) {
            case MAIN_MENU -> "<yellow>Maddox the Slayer</yellow>";
            case TIER_SELECTION, CONFIRM_PURCHASE -> slayerManager.getSlayerConfig().getString(selectedSlayerType + ".display-name", "Slayer Tiers");
            case LEVELING_REWARDS -> "Slayer Leveling Rewards";
            case BOSS_DROPS -> "Boss Drops";
            case RECIPES -> "Slayer Recipes";
        };
        return title;
    }

    @Override
    public int getSize() {
        return switch (currentMenu) {
            case MAIN_MENU -> 36;
            case TIER_SELECTION -> 54;
            case CONFIRM_PURCHASE, LEVELING_REWARDS, BOSS_DROPS -> 27;
            case RECIPES -> 36;
        };
    }

    @Override
    public void populateItems() {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) { inventory.setItem(i, filler); }

        int navButtonSlot = getSize() - 5;
        if (currentMenu == SlayerMenu.MAIN_MENU) {
            inventory.setItem(navButtonSlot, createItem(Material.BARRIER, "<red><b>Close</b></red>"));
        } else {
            inventory.setItem(navButtonSlot, createItem(Material.ARROW, "<green>Go Back</green>"));
        }

        switch (currentMenu) {
            case MAIN_MENU -> drawMainMenu();
            case TIER_SELECTION -> drawTierSelectionMenu();
            case CONFIRM_PURCHASE -> drawConfirmPurchaseMenu();
            case LEVELING_REWARDS -> drawLevelingRewardsMenu();
            case BOSS_DROPS -> drawBossDropsMenu();
//            case RECIPES -> drawRecipesMenu();
        }
    }

    // --- DRAW METHODS ---

    private void drawMainMenu() {
        SlayerManager sm = slayerManager;
        int[] slots = {10, 11, 12, 13, 14, 15}; // Z, S, W, E, B, C
        String[] slayerIds = {"ZOMBIE_SLAYER", "SPIDER_SLAYER", "WOLF_SLAYER", "ENDERMAN_SLAYER", "BLAZE_SLAYER"};

        for (int i = 0; i < slayerIds.length; i++) {
            String slayerId = slayerIds[i];
            ConfigurationSection config = sm.getSlayerConfig().getConfigurationSection(slayerId);
            if (config == null) {
                inventory.setItem(slots[i], createItem(Material.BEDROCK, "<gray>Coming Soon...</gray>"));
                continue;
            }
            String displayName = "⛧ " + config.getString("display-name");
            List<String> lore = List.of(" ", "<yellow>Click to view boss!");
            Material iconMaterial = Material.matchMaterial(config.getString("icon", "BARRIER"));
            inventory.setItem(slots[i], createItem(iconMaterial, displayName, lore));
        }

        inventory.setItem(slots[5], createItem(Material.BEDROCK, "<gray>To Be Continued...</gray>"));
        inventory.setItem(32, createItem(Material.NETHER_STAR, "<gold><b>Slayer Stats</b></gold>", List.of("<gray>Leveling up Slayers grants", "<gray>permanent stat boosts!")));
    }

    private void drawTierSelectionMenu() {
        PlayerSlayerData data = slayerDataManager.getData(player);
        int highestTierDefeated = data.getHighestTierDefeated(selectedSlayerType);
        String[] tierNames = {"Beginner", "Strong", "Advanced", "Very Hard", "Extreme"};
        int[] slots = {11, 12, 13, 14, 15};

        for (int i = 0; i < slots.length; i++) {
            int tier = i + 1;
            ConfigurationSection tierConfig = slayerManager.getSlayerConfig().getConfigurationSection(selectedSlayerType + ".tiers." + tier);
            if (tierConfig == null) continue;

            boolean isUnlocked = (tier <= highestTierDefeated + 1);
            if (!isUnlocked) {
                inventory.setItem(slots[i], createItem(Material.BEDROCK, "<red><b>Tier " + FormattingUtils.toRoman(tier) + "</b></red>", List.of("<dark_gray>??????????", " ", "<gray>Defeat the previous tier", "<gray>to unlock this boss.")));
                continue;
            }

            // --- FIX: Use the injected requirementManager ---
            boolean requirementsMet = requirementManager.meetsAll(player, tierConfig.getStringList("requirements"));
            String displayName = (requirementsMet ? "<yellow>" : "<red>") + slayerManager.getSlayerConfig().getString(selectedSlayerType + ".display-name") + " " + FormattingUtils.toRoman(tier);
            List<String> lore = new ArrayList<>();
            lore.add("<dark_gray>" + tierNames[i] + "</dark_gray>");
            lore.add(" ");
            lore.add("<gray>Health: <red>" + String.format("%,d", tierConfig.getInt("boss.health")) + "❤</red>");
            lore.add("<gray>Damage: <red>" + String.format("%,d", tierConfig.getInt("boss.damage")) + "</red>" + " per second");
            lore.add(" ");
            lore.add("<gray>Reward: <light_purple>" + tierConfig.getInt("slayer-xp-reward") + " " + selectedSlayerType.replace("_", " ") + " XP</light_purple>");
            lore.add("<dark_gray> + Boss Drops</dark_gray>");
            lore.add(" ");
            lore.add("<gray>Cost to start: <gold>" + String.format("%,d", tierConfig.getInt("start-cost")) + " coins</gold>");
            lore.add(" ");
            if (requirementsMet) { lore.add("<yellow>Click to Slay!</yellow>"); }
            else { lore.add("<red>Requirements not met!</red>"); }

            inventory.setItem(slots[i], createItem(Material.ENDER_EYE, displayName, lore));
        }

        String bossName = slayerManager.getSlayerConfig().getString(selectedSlayerType + ".display-name");
        int playerLevel = data.getLevel(selectedSlayerType);
        int playerXp = data.getXp(selectedSlayerType);
        int xpForPrevLevel = slayerManager.getSlayerConfig().getInt(selectedSlayerType + ".leveling-xp." + (playerLevel), 0);
        int xpForNextLevel = slayerManager.getSlayerConfig().getInt(selectedSlayerType + ".leveling-xp." + (playerLevel + 1), -1);

        // 'G' Button - Leveling Rewards
        List<String> bossItemLore = new ArrayList<>();
        bossItemLore.add("<dark_gray>" + bossName + " LVL</dark_gray>");
        bossItemLore.add(" ");
        bossItemLore.add("<dark_purple>1.</dark_purple> <gray>Kill boss to get XP</gray>");
        bossItemLore.add("<dark_purple>2.</dark_purple> <gray>Gain LVL from XP</gray>");
        bossItemLore.add("<dark_purple>3.</dark_purple> <gray>Unlock rewards per LVL</gray>");
        bossItemLore.add(" ");
        bossItemLore.add("<gray>Current LVL: </gray><yellow>" + FormattingUtils.toRoman(playerLevel) + "</yellow>");
        bossItemLore.add(" ");
        if (xpForNextLevel > 0) {
            int progressInLevel = playerXp - xpForPrevLevel;
            int neededForLevel = xpForNextLevel - xpForPrevLevel;
            bossItemLore.add("<gray>" + bossName + " XP to LVL " + FormattingUtils.toRoman(playerLevel + 1) + ":</gray>");
            bossItemLore.add(FormattingUtils.generateProgressBar(progressInLevel, neededForLevel) + "<light_purple>" + String.format("%,d", progressInLevel) + "</light_purple><dark_purple>/</dark_purple><light_purple>" + String.format("%,d", neededForLevel) + "</light_purple>");
        } else {
            bossItemLore.add("<gold>MAX LEVEL REACHED</gold>");
        }
        bossItemLore.add(" ");
        bossItemLore.add("<yellow>Click to view levels!</yellow>");
        inventory.setItem(29, createItem(Material.GOLD_BLOCK, "<dark_purple>Boss Leveling Rewards</dark_purple>", bossItemLore));

        // 'N' Button - Boss Drops
        List<String> bossDropItemLore = List.of("<dark_gray>" + bossName + "</dark_gray>", " ", "<gray>Usually, the boss will drop their main drop.", " ", "<gray>If you are lucky you may get something better", " ", "<yellow>Click to view drops!</yellow>");
        inventory.setItem(31, createItem(Material.GOLD_NUGGET, "<gold>Boss Drops</gold>", bossDropItemLore));

        // 'B' Button - Slayer Recipes
        int amountOfRecipes = 26; // Placeholder - this would come from a RecipeManager
        List<String> slayerRecipesItemLore = List.of("<dark_gray>" + bossName + "</dark_gray>", " ", "<gray>There are <yellow>" + amountOfRecipes + "</yellow> recipes related to the boss.", " ", "<gray>Unlock recipes and collect rare drops", "<gray>in order to craft powerful items.", " ", "<gray>Unlocked: <green>0</green> recipes", " ", "<yellow>Click to view recipes!</yellow>");
        inventory.setItem(33, createItem(Material.KNOWLEDGE_BOOK, "<green>Slayer Recipes</green>", slayerRecipesItemLore));
    }

    private void drawConfirmPurchaseMenu() {
        ConfigurationSection tierConfig = slayerManager.getSlayerConfig().getConfigurationSection(selectedSlayerType + ".tiers." + selectedTier);
        if (tierConfig == null) return;
        String cost = String.format("%,d", tierConfig.getInt("start-cost"));
        String bossName = slayerManager.getSlayerConfig().getString(selectedSlayerType + ".display-name");

        inventory.setItem(11, createItem(Material.GREEN_TERRACOTTA, "<green>Confirm</green>", List.of("<gray>Start Slayer Quest:", bossName + " " + FormattingUtils.toRoman(selectedTier), " ", "<gray>Cost: <gold>" + cost + " Coins</gold>", " ", "<yellow>Click to start quest!</yellow>")));
        inventory.setItem(15, createItem(Material.RED_TERRACOTTA, "<red>Cancel</red>"));
    }

    private void drawLevelingRewardsMenu() {
        PlayerSlayerData slayerData = slayerDataManager.getData(player);
        ConfigurationSection slayerConfig = slayerManager.getSlayerConfig().getConfigurationSection(selectedSlayerType);
        if (slayerData == null || slayerConfig == null) return;

        int playerLevel = slayerData.getLevel(selectedSlayerType);
        int playerXp = slayerData.getXp(selectedSlayerType);
        String slayerName = slayerConfig.getString("display-name", "Slayer");

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20}; // Levels 1-9
        for (int i = 0; i < slots.length; i++) {
            int level = i + 1;
            boolean isUnlocked = level <= playerLevel;
            boolean isRevealed = level <= playerLevel + 1;

            String title = "<gold>" + slayerName + " LVL " + FormattingUtils.toRoman(level) + "</gold>";
            List<String> lore = new ArrayList<>();

            if (!isRevealed) {
                // --- THIS IS THE LOCKED/SECRET VIEW ---
                lore.add(" ");
                lore.add("<gray>Reward is secret!</gray>");
                lore.add("<yellow>Reach LVL " + FormattingUtils.toRoman(level - 1) + " to reveal!</yellow>");
                inventory.setItem(slots[i], createItem(Material.BEDROCK, title, lore));
                continue;
            }

            // --- THIS IS THE REVEALED VIEW (UNLOCKED OR IN-PROGRESS) ---
            Material mat = isUnlocked ? Material.GREEN_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE;

            lore.add("<gray>Rewards:");
            List<String> rewardStrings = slayerConfig.getStringList("rewards." + level);
            if (rewardStrings.isEmpty()) {
                lore.add("<dark_gray>None");
            } else {
                rewardStrings.forEach(r -> lore.add(ChatUtils.formatRewardString(r)));
            }
            lore.add(" ");
            lore.add("<gray>Progress:</gray>");

            int xpForThisLevel = slayerConfig.getInt("leveling-xp." + level, 0);
            int xpForPrevLevel = slayerConfig.getInt("leveling-xp." + (level - 1), 0);
            int neededForLevel = xpForThisLevel - xpForPrevLevel;
            int progressInLevel = isUnlocked ? neededForLevel : playerXp - xpForPrevLevel;

            lore.add(FormattingUtils.generateProgressBar(progressInLevel, neededForLevel) + " <gray>" + String.format("%,d", progressInLevel) + "/" + String.format("%,d", neededForLevel));
            lore.add(" ");
            lore.add("<gray>Kill the boss to gain XP.");
            lore.add("<gray>Higher tiers reward more XP.");

            inventory.setItem(slots[i], createItem(mat, title, lore));
        }
    }

    private void drawBossDropsMenu() {
        PlayerSlayerData slayerData = slayerDataManager.getData(player);
        int tierToShow = Math.max(1, slayerData.getHighestTierDefeated(selectedSlayerType));
        ConfigurationSection lootConfig = slayerManager.getSlayerConfig().getConfigurationSection(selectedSlayerType + ".tiers." + tierToShow + ".boss.loot");

        inventory.setItem(4, createItem(Material.DIAMOND, "<white><b>Showing Drops for Tier " + FormattingUtils.toRoman(tierToShow) + "</b></white>"));
        if (lootConfig == null) return;

        int currentSlot = 10;
        // Guaranteed Drops
        for (String dropString : lootConfig.getStringList("guaranteed")) {
            if (currentSlot > 16) break;
            inventory.setItem(currentSlot++, createItem(Material.IRON_INGOT, "<white>" + dropString, List.of("<green>Guaranteed Drop</green>")));
        }
        // Rare Drops
        ConfigurationSection rarePool = lootConfig.getConfigurationSection("rare-pool");
        if (rarePool != null) {
            for (String itemKey : rarePool.getKeys(false)) {
                if (currentSlot > 16) break;
                double chance = rarePool.getDouble(itemKey);
                inventory.setItem(currentSlot++, createItem(Material.GOLD_INGOT, "<gold>" + itemKey, List.of(getChanceRarity(chance))));
            }
        }
    }

    private void drawComingSoon() {
        inventory.setItem(22, createItem(Material.BARRIER, "<red>Coming Soon</red>"));
    }

    // --- CLICK HANDLERS ---

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        if (slot == getSize() - 5) { handleBackClick(); return; }

        switch (currentMenu) {
            case MAIN_MENU -> handleMainMenuClick(slot);
            case TIER_SELECTION -> handleTierSelectionClick(slot);
            case CONFIRM_PURCHASE -> handleConfirmPurchaseClick(slot);
        }
    }

    private void handleBackClick() {
        switch (currentMenu) {
            case MAIN_MENU -> player.closeInventory();
            case CONFIRM_PURCHASE -> {
                this.currentMenu = SlayerMenu.TIER_SELECTION;
                reopen(); // Re-open the GUI with the new size
            }
            default -> {
                this.currentMenu = SlayerMenu.MAIN_MENU;
                reopen(); // Re-open the GUI with the new size
            }
        }
    }

    private void handleTierSelectionClick(int slot) {
        SlayerMenu nextMenu = null;
        if (slot == 29) nextMenu = SlayerMenu.LEVELING_REWARDS;
        else if (slot == 31) nextMenu = SlayerMenu.BOSS_DROPS;
        else if (slot == 33) nextMenu = SlayerMenu.RECIPES;
        else {
            int[] slots = {11, 12, 13, 14, 15};
            for (int i = 0; i < slots.length; i++) {
                if (slot == slots[i]) {
                    this.selectedTier = i + 1;
                    nextMenu = SlayerMenu.CONFIRM_PURCHASE;
                    break;
                }
            }
        }

        if (nextMenu != null) {
            this.currentMenu = nextMenu;
            reopen(); // Re-open the GUI with the new size
        }
    }

    private void handleMainMenuClick(int slot) {
        int[] slots = {10, 11, 12, 13, 14};
        String[] slayerIds = {"ZOMBIE_SLAYER", "SPIDER_SLAYER", "WOLF_SLAYER", "ENDERMAN_SLAYER", "BLAZE_SLAYER"};

        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i]) {
                if (slayerManager.getSlayerConfig().isConfigurationSection(slayerIds[i])) {
                    this.selectedSlayerType = slayerIds[i];
                    this.currentMenu = SlayerMenu.TIER_SELECTION;
                    reopen(); // Re-open the GUI with the new size
                }
                return;
            }
        }
    }


    private void handleConfirmPurchaseClick(int slot) {
        if (slot != 11) {
            this.currentMenu = SlayerMenu.TIER_SELECTION;
            reopen();
            return;
        }

        ConfigurationSection tierConfig = slayerManager.getSlayerConfig().getConfigurationSection(selectedSlayerType + ".tiers." + selectedTier);
        if (tierConfig == null) return;

        if (slayerManager.hasActiveQuest(player)) {
            player.sendMessage(ChatUtils.format("<red>You already have an active slayer quest!</red>"));
            return;
        }

        // --- THIS IS THE FIX ---
        // Use the injected requirementManager to check the requirements again.
        if (!requirementManager.meetsAll(player, tierConfig.getStringList("requirements"))) {
            player.sendMessage(ChatUtils.format("<red>You no longer meet the requirements for this tier!</red>"));
            return;
        }

        int cost = tierConfig.getInt("start-cost");
        if (economyManager.getEconomy(player).getPurse() < cost) {
            player.sendMessage(ChatUtils.format("<red>You cannot afford this quest!</red>"));
            return;
        }

        economyManager.getEconomy(player).removePurse(cost);
        slayerManager.startQuest(player, selectedSlayerType, selectedTier);
        player.sendMessage(ChatUtils.format("<green>Slayer quest started!</green>"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.closeInventory();
    }

    // --- UTILITY METHODS ---

    private String getChanceRarity(double chance) {
        if (chance >= 0.5) return "<yellow>Uncommon Drop</yellow>";
        if (chance >= 0.1) return "<blue>Rare Drop</blue>";
        if (chance >= 0.01) return "<dark_purple>Very Rare Drop</dark_purple>";
        return "<light_purple><b>Extremely Rare Drop</b></light_purple>";
    }

    /**
     * A utility method to smoothly transition between different menu states
     * by closing the current GUI and opening a new instance of itself.
     * This is necessary to handle dynamic inventory resizing.
     */
    private void reopen() {
        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                SlayerGui newGui = new SlayerGui(plugin, player, currentMenu, selectedSlayerType, selectedTier, slayerManager, slayerDataManager, economyManager, requirementManager);
                newGui.open();
            }
        }.runTaskLater(plugin, 1L);
    }
}