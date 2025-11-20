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
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.util.ChatUtils;
import org.nakii.mmorpg.util.FormattingUtils;

import java.util.ArrayList;
import java.util.List;

public class SkillsGui extends AbstractGui {

    private enum View { OVERVIEW, DETAIL }
    private View currentView = View.OVERVIEW;
    private Skill selectedSkill = null;
    private final SkillManager skillManager;

    private static final int[] SNAKE_PATH = {
            9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17
    };

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
        if (skillConfig == null) return; // Failsafe
        int maxLevel = skillConfig.getInt("max-level", 60);

        this.maxItemsPerPage = SNAKE_PATH.length;
        this.totalPages = (int) Math.ceil((double) maxLevel / maxItemsPerPage);

        int startingLevel = (page * maxItemsPerPage) + 1;

        for (int i = 0; i < SNAKE_PATH.length; i++) {
            int slot = SNAKE_PATH[i];

            if (page == 0 && i == 0) {
                inventory.setItem(slot, createItem(
                        Material.NETHER_STAR,
                        "<yellow>Skill Unlocked</yellow>",
                        List.of("<gray>Beginning of your journey.", "<gray>Gain XP to level up!</gray>")
                ));
                continue;
            }

            int currentLevel = startingLevel + i - (page == 0 ? 1 : 0);

            if (currentLevel > maxLevel) {
                inventory.setItem(slot, null);
                continue;
            }

            boolean isUnlocked = currentLevel <= playerLevel;
            Material mat = isUnlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String name = (isUnlocked ? "<green>" : "<red>") + "Level " + FormattingUtils.toRoman(currentLevel);

            List<String> lore = new ArrayList<>();
            lore.add("<white>Rewards for this Level:</white>");
            boolean hasRewards = false;

            // Universal coin reward
            int coins = skillManager.getLevelsConfig().getInt("levels." + currentLevel + ".coins", 0);
            if (coins > 0) {
                // Use the centralized formatter for coins
                lore.add(ChatUtils.formatRewardString("COINS:" + coins));
                hasRewards = true;
            }

            // Per-level stat rewards
            ConfigurationSection rewardsPerLevel = skillConfig.getConfigurationSection("rewards-per-level");
            if (rewardsPerLevel != null) {
                for (String statKey : rewardsPerLevel.getKeys(false)) {
                    try {
                        Stat stat = Stat.valueOf(statKey.toUpperCase());
                        double bonusPerLevel = rewardsPerLevel.getDouble(statKey);
                        // Use the stat's own powerful .format() method
                        lore.add("<gray>• " + stat.format(bonusPerLevel) + "</gray>");
                        hasRewards = true;
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            // Milestone rewards
            List<String> milestoneRewards = skillConfig.getStringList("milestone-rewards." + currentLevel);
            if (!milestoneRewards.isEmpty()) {
                // This was already correct, using the centralized formatter
                milestoneRewards.forEach(r -> lore.add(ChatUtils.formatRewardString(r)));
                hasRewards = true;
            }

            if (!hasRewards) {
                lore.add("<dark_gray>None</dark_gray>");
            }

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

            String displayName = skill.getSymbol() + " " + config.getString("display-name", skill.name());
            Material icon = Material.matchMaterial(config.getString("icon", "BARRIER"));
            List<String> lore = new ArrayList<>();

            // --- 1. Main Level Display ---
            if (level >= maxLevel) {
                lore.add("<gold><b>MAX LEVEL (" + maxLevel + ")</b></gold>");
            } else {
                lore.add("<gray>Level <yellow>" + level + "</yellow>/" + maxLevel);
            }
            lore.add(" ");

            // --- 2. Progress Bar (if not max level) ---
            if (level < maxLevel) {
                double xpForCurrentLevel = skillManager.getCumulativeXpForLevel(level);
                double xpForNextLevel = skillManager.getCumulativeXpForLevel(level + 1);
                double progressInLevel = currentTotalXp - xpForCurrentLevel;
                double neededForLevel = xpForNextLevel - xpForCurrentLevel;

                lore.add("<white>Progress to Lvl " + (level + 1) + ": <yellow>" + String.format("%.1f%%", (progressInLevel / neededForLevel) * 100) + "</yellow>");
                lore.add(FormattingUtils.generateProgressBar(progressInLevel, neededForLevel) + " <gray>" + String.format("%,.0f", progressInLevel) + "/" + String.format("%,.0f", neededForLevel));
                lore.add(" ");
            }

            // --- 3. Total Stats Gained from this Skill ---
            lore.add("<white>Total Bonuses from " + ChatUtils.capitalizeWords(skill.name()) + ":</white>");
            ConfigurationSection rewardsPerLevel = config.getConfigurationSection("rewards-per-level");
            if (rewardsPerLevel != null && level > 0) {
                for (String statKey : rewardsPerLevel.getKeys(false)) {
                    try {
                        Stat stat = Stat.valueOf(statKey.toUpperCase());
                        double bonusPerLevel = rewardsPerLevel.getDouble(statKey);
                        double totalBonus = level * bonusPerLevel;
                        lore.add("<gray>• " + stat.format(totalBonus) + "</gray>");
                    } catch (IllegalArgumentException ignored) {}
                }
            } else {
                lore.add("<dark_gray>None</dark_gray>");
            }
            lore.add(" ");

            // --- 4. Next Milestone Reward ---
            lore.add("<white>Next Milestone Reward:</white>");
            ConfigurationSection milestoneSection = config.getConfigurationSection("milestone-rewards");
            if (milestoneSection != null) {
                int nextMilestoneLevel = milestoneSection.getKeys(false).stream()
                        .mapToInt(Integer::parseInt)
                        .filter(lvl -> lvl > level)
                        .min()
                        .orElse(-1);

                if (nextMilestoneLevel != -1) {
                    lore.add("<gray>At Level <yellow>" + nextMilestoneLevel + "</yellow>:");
                    String nextRewardString = milestoneSection.getStringList(String.valueOf(nextMilestoneLevel)).stream().findFirst().orElse("Unknown Reward");
                    // <<< FIX: Use the new centralized ChatUtils formatter >>>
                    lore.add(ChatUtils.formatRewardString(nextRewardString));
                } else {
                    lore.add("<green>✔ All milestones unlocked!</green>");
                }
            } else {
                lore.add("<dark_gray>None</dark_gray>");
            }
            lore.add(" ");
            lore.add("<yellow>Click to view all rewards!</yellow>");

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
        // When the back button is clicked, we transition to the OVERVIEW state
        if (event.getSlot() == getSize() - 5) {
            this.selectedSkill = null; // Clear the selected skill
            this.currentView = View.OVERVIEW; // Set the view state
            reopen(); // Reopen the GUI in its new state
        } else {
            // Handle other clicks, like pagination
            super.handleClick(event);
        }
    }

    /**
     * Re-opens the GUI to reflect a change in state (like view or page).
     * This method now correctly chooses the constructor based on the current view.
     */
    private void reopen() {
        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentView == View.DETAIL && selectedSkill != null) {
                    new SkillsGui(plugin, player, skillManager, selectedSkill).open();
                } else {
                    new SkillsGui(plugin, player, skillManager).open();
                }
            }
        }.runTaskLater(plugin, 1L);
    }
}