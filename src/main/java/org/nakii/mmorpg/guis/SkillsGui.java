package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.SkillManager;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.util.FormattingUtils;

import java.util.ArrayList;
import java.util.List;

public class SkillsGui extends AbstractGui {

    private enum View { OVERVIEW, DETAIL }
    private View currentView = View.OVERVIEW;
    private Skill selectedSkill = null;
    private final SkillManager skillManager;

    // --- USING YOUR EXACT SNAKE PATH ---
    private static final int[] SNAKE_PATH = {
            9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17
    };
    // Note: This path has 20 slots. Page 1 = Lvl 1-20, Page 2 = 21-40, Page 3 = 41-60.

    public SkillsGui(MMORPGCore plugin, Player player, SkillManager skillManager) {
        super(plugin, player);
        this.skillManager = skillManager;
    }

    public SkillsGui(MMORPGCore plugin, Player player, SkillManager skillManager, Skill selectedSkill) {
        super(plugin, player);
        this.skillManager = skillManager;
        this.selectedSkill = selectedSkill;
        this.currentView = View.DETAIL;
    }

    @Override
    public @NotNull String getTitle() {
        if (currentView == View.DETAIL && selectedSkill != null) {
            String skillName = skillManager.getSkillsConfig().getString(selectedSkill.name() + ".display-name", "Skill");
            return skillName;
        }
        return "Your Skills";
    }

    @Override
    public int getSize() {
        return currentView == View.OVERVIEW ? 36 : 54;
    }

    @Override
    public void populateItems() {
        // --- FIX #1: MANUAL LAYOUT CONTROL ---
        // We no longer call drawBaseLayout() to prevent it from overwriting our edge slots.
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) {
            inventory.setItem(i, filler);
        }

        if (currentView == View.OVERVIEW) {
            drawOverview();
        } else {
            drawDetailView();
        }

        // Draw navigation buttons AFTER the main content to ensure they are on top.
        int closeSlot = getSize() - 5;
        int prevPageSlot = getSize() - 6;
        int nextPageSlot = getSize() - 4;

        if (currentView == View.DETAIL) {
            if (page > 0) inventory.setItem(prevPageSlot, createItem(Material.ARROW, "<green>Previous Page</green>"));
            if (page < totalPages - 1) inventory.setItem(nextPageSlot, createItem(Material.ARROW, "<green>Next Page</green>"));
            inventory.setItem(closeSlot, createItem(Material.ARROW, "<green>Go Back</green>"));
        } else {
            inventory.setItem(closeSlot, createItem(Material.BARRIER, "<red><b>Close</b></red>"));
        }
    }

    private void drawDetailView() {
        int playerLevel = skillManager.getLevel(player, selectedSkill);
        ConfigurationSection skillConfig = skillManager.getSkillsConfig().getConfigurationSection(selectedSkill.name());
        int maxLevel = skillConfig.getInt("max-level", 60);

        this.maxItemsPerPage = SNAKE_PATH.length;
        this.totalPages = (int) Math.ceil((double) maxLevel / maxItemsPerPage);

        int startingLevel = (page * maxItemsPerPage) + 1;

        for(int i = 0; i < maxItemsPerPage; i++) {

            int slot = SNAKE_PATH[i];

            if (page == 0 && i == 0) {
                ItemStack startItem = createItem(
                        Material.NETHER_STAR,
                        "<yellow>Level 0 (Start)",
                        List.of("<gray>Beginning of your journey</gray>")
                );
                inventory.setItem(slot, startItem);
                continue;
            }

            int currentLevel = startingLevel + i;
            if (currentLevel > maxLevel) break;

            boolean isUnlocked = currentLevel <= playerLevel;
            Material mat = isUnlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String name = (isUnlocked ? "<green>" : "<red>") + "Level " + currentLevel;

            List<String> lore = new ArrayList<>();
            lore.add("<white>Rewards:");
            int coins = skillManager.getLevelsConfig().getInt("levels." + currentLevel + ".coins", 0);
            if (coins > 0) lore.add("<gold>+ " + String.format("%,d", coins) + " Coins</gold>");

            ConfigurationSection rewardsPerLevel = skillConfig.getConfigurationSection("rewards-per-level");
            if(rewardsPerLevel != null) {
                for(String statKey : rewardsPerLevel.getKeys(false)) {
                    lore.add("<gray>+ <green>" + rewardsPerLevel.getDouble(statKey) + " " + statKey.replace("_", " ") + "</green>");
                }
            }
            skillConfig.getStringList("milestone-rewards." + currentLevel).forEach(r -> lore.add("<gold>" + formatRewardString(r) + "</gold>"));

            ItemStack item = createItem(mat, name, lore);
            item.setAmount(Math.min(64, currentLevel));
            inventory.setItem(slot, item);
        }
    }

    private void drawOverview() {
        PlayerSkillData data = skillManager.getPlayerData(player);
        ConfigurationSection skillsConfig = skillManager.getSkillsConfig();

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19};
        Skill[] skillOrder = {Skill.FARMING, Skill.MINING, Skill.COMBAT, Skill.FORAGING, Skill.FISHING, Skill.ENCHANTING, Skill.ALCHEMY, Skill.CARPENTRY};

        for (int i = 0; i < skillOrder.length; i++) {
            Skill skill = skillOrder[i];
            ConfigurationSection config = skillsConfig.getConfigurationSection(skill.name());
            if (config == null) continue;

            int level = data.getLevel(skill);
            double currentTotalXp = data.getXp(skill);
            int maxLevel = config.getInt("max-level", 60);

            String displayName = config.getString("display-name", skill.name());
            Material icon = Material.matchMaterial(config.getString("icon", "BARRIER"));
            List<String> lore = new ArrayList<>();
            lore.add("<gray>Level " + level + "</gray>");
            lore.add(" ");

            if (level < maxLevel) {
                double xpForCurrentLevel = skillManager.getCumulativeXpForLevel(level);
                double xpForNextLevel = skillManager.getCumulativeXpForLevel(level + 1);
                double progressInLevel = currentTotalXp - xpForCurrentLevel;
                double neededForLevel = xpForNextLevel - xpForCurrentLevel;
                lore.add("<white>Progress to Level " + (level + 1) + ": <yellow>" + String.format("%.1f%%", (progressInLevel / neededForLevel) * 100) + "</yellow>");
                lore.add(FormattingUtils.generateProgressBar(progressInLevel, neededForLevel) + " <gray>" + String.format("%,.0f", progressInLevel) + "/" + String.format("%,.0f", neededForLevel));
            } else {
                lore.add("<gold>MAX LEVEL</gold>");
            }
            lore.add(" ");
            lore.add("<white>Rewards per Level:");
            config.getConfigurationSection("rewards-per-level").getKeys(false).forEach(statKey ->
                    lore.add("<gray>+ <green>" + config.getDouble("rewards-per-level." + statKey) + " " + statKey.replace("_", " ") + "</green>")
            );
            lore.add(" ");
            lore.add("<yellow>Click to view details!</yellow>");

            inventory.setItem(slots[i], createItem(icon, displayName, lore));
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (currentView == View.OVERVIEW) {
            handleOverviewClick(event);
        } else {
            handleDetailViewClick(event);
        }
    }

    private void handleOverviewClick(InventoryClickEvent event) {
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19};
        Skill[] skillOrder = {Skill.FARMING, Skill.MINING, Skill.COMBAT, Skill.FORAGING, Skill.FISHING, Skill.ENCHANTING, Skill.ALCHEMY, Skill.CARPENTRY};

        for (int i = 0; i < slots.length; i++) {
            if (event.getSlot() == slots[i]) {
                this.selectedSkill = skillOrder[i];
                this.currentView = View.DETAIL;
                reopen();
                return;
            }
        }
        super.handleClick(event);
    }

    private void handleDetailViewClick(InventoryClickEvent event) {
        if (event.getSlot() == getSize() - 5) { // Back button
            this.selectedSkill = null;
            this.currentView = View.OVERVIEW;
            reopen();
        } else {
            super.handleClick(event);
        }
    }

    private void reopen() {
        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                new SkillsGui(plugin, player, skillManager, selectedSkill).open();
            }
        }.runTaskLater(plugin, 1L);
    }

    private String formatRewardString(String rewardString) {
        String[] parts = rewardString.split(":");
        if (parts.length < 2) return rewardString;
        return "• " + parts[0].replace("_", " ") + ": " + parts[1].replace("_", " ");
    }
}